/*
 * Copyright 2020 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.milton.grizzly;

import io.milton.http.FileItem;
import io.milton.servlet.FileItemWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.util.FileItemHeadersImpl;
import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.http.io.NIOInputStream;
import org.glassfish.grizzly.http.io.NIOReader;
import org.glassfish.grizzly.http.multipart.ContentDisposition;
import org.glassfish.grizzly.http.multipart.MultipartEntry;
import org.glassfish.grizzly.http.multipart.MultipartEntryHandler;
import org.glassfish.grizzly.http.multipart.MultipartScanner;
import org.glassfish.grizzly.http.server.Request;

/**
 *
 * @author dylan
 */
public class MiltonGrizzlyMultipartUploader {

	private final Map<String, String> params;
	private final Map<String, FileItem> files;
	private final FileItemFactory fileItemFactory;

	public MiltonGrizzlyMultipartUploader(Map<String, String> params, Map<String, FileItem> files, FileItemFactory fileItemFactory) {
		this.params = params;
		this.files = files;
		this.fileItemFactory = fileItemFactory;
	}

	public void parseRequest(final Request request) throws Throwable {
		AsyncResult ar = AsyncRunnable.wait(new AsyncRunnable<AsyncResult>() {
			@Override
			public void run(final AtomicReference<AsyncResult> notifier) {
				MultipartScanner.scan(request,
						new MiltonMultipartEntryHandler(params, files, fileItemFactory, notifier),
						new EmptyCompletionHandler<Request>() {
					@Override
					public void cancelled() {
						finish(notifier, new AsyncResult(null));
					}

					@Override
					public void completed(Request result) {
						finish(notifier, new AsyncResult(null));
					}

					@Override
					public void failed(Throwable throwable) {
						finish(notifier, new AsyncResult(throwable));
					}
				});
			}
		});

		if (!ar.status) {
			if (ar.error instanceof Exception) {
				throw (Exception) ar.error;
			} else {
				throw new Exception(ar.error.getMessage(), ar.error);
			}
		}
	}

	public void parseMultipart(final MultipartEntry multipartEntry, final AtomicReference<AsyncResult> notifier) throws Throwable {
		MultipartScanner.scan(multipartEntry,
				new MiltonMultipartEntryHandler(params, files, fileItemFactory, notifier),
				new EmptyCompletionHandler<MultipartEntry>() {
			@Override
			public void failed(Throwable throwable) {
				synchronized (notifier) {
					notifier.set(new AsyncResult(throwable));
					notifier.notify();
				}
			}
		});
	}

	/**
	 *
	 */
	private class MiltonMultipartEntryHandler implements MultipartEntryHandler {

		private final Map<String, String> params;
		private final Map<String, FileItem> files;
		private final FileItemFactory fileItemFactory;
		private final AtomicReference<AsyncResult> notifier;

		private int paramCount = 0;
		private int fileCount = 0;

		public MiltonMultipartEntryHandler(Map<String, String> params, Map<String, FileItem> files, FileItemFactory fileItemFactory, final AtomicReference<AsyncResult> notifier) {
			this.params = params;
			this.files = files;
			this.fileItemFactory = fileItemFactory;
			this.notifier = notifier;
		}

		@Override
		public void handle(MultipartEntry multipartEntry) throws Exception {
			String multipartFileName = null;
			String multipartName = null;

			ContentDisposition contentDisposition = multipartEntry.getContentDisposition();
			if (contentDisposition != null) {
				multipartFileName = contentDisposition.getDispositionParamUnquoted("filename");
				multipartName = contentDisposition.getDispositionParamUnquoted("name");
			}

			if (multipartEntry.isMultipart()) {
				// Parse child multipart - Very uncommon, But still possible
				try {
					parseMultipart(multipartEntry, notifier);
				} catch (Throwable ex) {
					if (ex instanceof Exception) {
						throw (Exception) ex;
					} else {
						throw new Exception(ex.getMessage(), ex);
					}
				}
			} else if (StringUtils.isNotBlank(multipartFileName)) {
				// Parse file
				if (StringUtils.isBlank(multipartName)) {
					multipartName = "file_" + fileCount++;
				}

				String itemKey = multipartName;
				if (files.containsKey(itemKey)) {
					int count = 1;
					while (files.containsKey(itemKey + count)) {
						count++;
					}
					itemKey = itemKey + count;
				}

				final org.apache.commons.fileupload.FileItem f = fileItemFactory.createItem(multipartName, multipartEntry.getContentType(), false, multipartFileName);

				FileItemHeadersImpl headers = new FileItemHeadersImpl();

				f.setHeaders(headers);

				for (String headerName : multipartEntry.getHeaderNames()) {
					headers.addHeader(headerName, multipartEntry.getHeader(headerName));
				}

				final String mpn = itemKey;
				final OutputStream outputStream = f.getOutputStream();
				final NIOInputStream stream = multipartEntry.getNIOInputStream();

				stream.notifyAvailable(new UploadReadHandler(stream, outputStream, new Consumer<Throwable>() {
					@Override
					public void accept(Throwable t) {
						files.put(mpn, new FileItemWrapper(f));
					}
				}));
			} else {
				// Parse Param
				if (StringUtils.isBlank(multipartName)) {
					multipartName = "param_" + paramCount++;
				}

				final String mpn = multipartName;
				final NIOReader nioReader = multipartEntry.getNIOReader();

				nioReader.notifyAvailable(new ReadHandler() {
					@Override
					public void onDataAvailable() throws Exception {
						// ignored
					}

					@Override
					public void onError(Throwable t) {
						try {
							nioReader.close();
						} catch (IOException e) {
						}
						synchronized (notifier) {
							notifier.set(new AsyncResult(t));
							notifier.notify();
						}
					}

					@Override
					public void onAllDataRead() throws Exception {
						final char[] chars = new char[nioReader.readyData()];
						nioReader.read(chars);
						String s = new String(chars);
						params.put(mpn, s);
					}
				});
			}
		}
	}

	private static class UploadReadHandler implements ReadHandler {

		// Non-blocking multipart entry input stream
		private final NIOInputStream inputStream;

		// the destination file output stream, where we save the data.
		private final OutputStream outputStream;

		// the callback to call when finished
		private final Consumer<Throwable> cb;

		// temporary buffer
		private final byte[] buf;

		public UploadReadHandler(NIOInputStream inputStream, OutputStream outputStream, Consumer<Throwable> cb) {
			this.inputStream = inputStream;
			this.outputStream = outputStream;
			this.cb = cb;

			this.buf = new byte[2048];
		}

		@Override
		public void onDataAvailable() throws Exception {
			// save available file content
			readAndSaveAvail();

			// register this handler to be notified next time some data
			// becomes available
			inputStream.notifyAvailable(UploadReadHandler.this);
		}

		@Override
		public void onError(Throwable t) {
			finish(t);
		}

		@Override
		public void onAllDataRead() throws Exception {
			// save available file content
			readAndSaveAvail();
			// finish the upload
			finish(null);
		}

		private void readAndSaveAvail() throws IOException {
			while (inputStream.isReady()) {
				// read the available bytes from input stream
				final int readBytes = inputStream.read(buf);
				// save the file content to the file
				outputStream.write(buf, 0, readBytes);
			}
		}

		/**
		 * Finish the file upload
		 */
		private void finish(Throwable t) {
			try {
				// close file output stream
				outputStream.close();
			} catch (IOException ignored) {
			}

			this.cb.accept(t);
		}
	}

	/**
	 * A class to run asynchronous tasks in a synchronous process
	 */
	private static abstract class AsyncRunnable<T> {

		protected abstract void run(final AtomicReference<T> notifier);

		protected final void finish(final AtomicReference<T> notifier, T result) {
			synchronized (notifier) {
				notifier.set(result);
				notifier.notify();
			}
		}

		public static <T> T wait(final AsyncRunnable<T> runnable) throws InterruptedException {
			final AtomicReference<T> notifier = new AtomicReference<T>();

			// run the asynchronous code
			runnable.run(notifier);

			// wait for the asynchronous code to finish
			synchronized (notifier) {
				while (notifier.get() == null) {
					try {
						notifier.wait();
					} catch (InterruptedException e) {
						throw e;
					}
				}
			}

			// return the result of the asynchronous code
			return notifier.get();
		}
	}

	private class AsyncResult {

		public AsyncResult(Throwable error) {
			this.status = error == null;
			this.error = error;
		}

		boolean status;
		Throwable error;
	}
}
