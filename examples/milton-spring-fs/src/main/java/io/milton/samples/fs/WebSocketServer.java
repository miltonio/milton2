package io.milton.samples.fs;

import io.milton.http.WSManager;
import io.milton.webdav.utils.StringUtil;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

/**
 * WebSocket server, creates web socket endpoint, handles client's sessions
 */
public class WebSocketServer implements WSManager {

    private List<WebSocketSession> sessions;

    public WebSocketServer(SocketHandler socketHandler) {
        this.sessions = socketHandler.getSessions();
    }

    /**
     * Send notification to the client
     *
     * @param itemPath   File/Folder path.
     * @param operation  Operation name: created/updated/deleted/moved
     */
    private void send(String itemPath, String operation) {
        itemPath = StringUtil.trimEnd(StringUtil.trimStart(itemPath, "/"), "/");
        for (WebSocketSession session: sessions) {
            try {
                session.sendMessage(new TextMessage(new Notification(itemPath, operation).toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Notifies client that file/folder was created.
     *
     * @param itemPath file/folder.
     */
    public void notifyCreated(String itemPath) {
        send(itemPath, "created");
    }

    /**
     * Notifies client that file/folder was updated.
     *
     * @param itemPath file/folder.
     */
    public void notifyUpdated(String itemPath) {
        send(itemPath, "updated");
    }

    /**
     * Notifies client that file/folder was deleted.
     *
     * @param itemPath file/folder.
     */
    public void notifyDeleted(String itemPath) {
        send(itemPath, "deleted");
    }

    /**
     * Notifies client that file/folder was locked.
     *
     * @param itemPath file/folder.
     */
    public void notifyLocked(String itemPath) {
        send(itemPath, "locked");
    }

    /**
     * Notifies client that file/folder was unlocked.
     *
     * @param itemPath file/folder.
     */
    public void notifyUnlocked(String itemPath) {
        send(itemPath, "unlocked");
    }

    /**
     * Notifies client that file/folder was moved.
     *
     * @param itemPath file/folder.
     */
    public void notifyMoved(String itemPath, String targetPath) {
        itemPath = StringUtil.trimEnd(StringUtil.trimStart(itemPath, "/"), "/");
        targetPath = StringUtil.trimEnd(StringUtil.trimStart(targetPath, "/"), "/");
        for (WebSocketSession session: sessions) {
            try {
                session.sendMessage(new TextMessage(new MovedNotification(itemPath, "moved", targetPath).toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Represents VO to exchange between client and server
     */
    static class Notification {
        protected final String itemPath;
        protected final String operation;

        Notification(String itemPath, String operation) {
            this.itemPath = itemPath;
            this.operation = operation;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"ItemPath\" : \"" + itemPath + "\" ," +
                    "\"EventType\" : \"" + operation + "\"" +
                    "}";
        }
    }

    /**
     * Represents VO to exchange between client and server for move type
     */
    static class MovedNotification extends Notification {
        private final String targetPath;

        MovedNotification(String itemPath, String operation, String targetPath) {
            super(itemPath, operation);
            this.targetPath = targetPath;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"ItemPath\" : \"" + itemPath + "\" ," +
                    "\"TargetPath\" : \"" + targetPath + "\" ," +
                    "\"EventType\" : \"" + operation + "\"" +
                    "}";
        }

    }
}
