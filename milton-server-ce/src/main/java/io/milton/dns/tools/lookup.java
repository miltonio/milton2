/*
 * Copied from the DnsJava project
 *
 * Copyright (c) 1998-2011, Brian Wellington.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package io.milton.dns.tools;

import io.milton.dns.Name;
import io.milton.dns.record.Lookup;
import io.milton.dns.record.Record;
import io.milton.dns.record.Type;

import io.milton.dns.*;

/**
 * @author Brian Wellington &lt;bwelling@xbill.org&gt;
 */
public class lookup {

	public static void printAnswer(String name, Lookup lookup) {
		System.out.print(name + ":");
		int result = lookup.getResult();
		if (result != Lookup.SUCCESSFUL) {
			System.out.print(" " + lookup.getErrorString());
		}
		System.out.println();
		Name[] aliases = lookup.getAliases();
		if (aliases.length > 0) {
			System.out.print("# aliases: ");
			for (int i = 0; i < aliases.length; i++) {
				System.out.print(aliases[i]);
				if (i < aliases.length - 1) {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		if (lookup.getResult() == Lookup.SUCCESSFUL) {
			Record[] answers = lookup.getAnswers();
			for (int i = 0; i < answers.length; i++) {
				System.out.println(answers[i]);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		int type = Type.A;
		int start = 0;
		if (args.length > 2 && args[0].equals("-t")) {
			type = Type.value(args[1]);
			if (type < 0) {
				throw new IllegalArgumentException("invalid type");
			}
			start = 2;
		}
		for (int i = start; i < args.length; i++) {
			Lookup l = new Lookup(args[i], type);
			l.run();
			printAnswer(args[i], l);
		}
	}
}
