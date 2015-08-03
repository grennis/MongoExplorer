package com.innodroid.mongobrowser;

import de.greenrobot.event.EventBus;

public class Events {
    public static void postAddConnection() {
        EventBus.getDefault().post(new AddConnection());
    }

    public static void postConnectionSelected(long connectionID) {
        EventBus.getDefault().post(new ConnectionSelected(connectionID));
    }

    public static void postConnected(long connectionID) {
        EventBus.getDefault().post(new Connected(connectionID));
    }

    public static void postConnectionDeleted() {
        EventBus.getDefault().post(new ConnectionDeleted());
    }

    public static void postCollectionSelected(long connectionId, String collectionName) {
        EventBus.getDefault().post(new CollectionSelected(connectionId, collectionName));
    }

    public static void postAddDocument() {
        EventBus.getDefault().post(new AddDocument());
    }

    public static void postDocumentSelected(String content) {
        EventBus.getDefault().post(new DocumentSelected(content));
    }

    public static void postDocumentClicked(String content) {
        EventBus.getDefault().post(new DocumentClicked(content));
    }

    public static void postCreateCollection(String name) {
        EventBus.getDefault().post(new CreateCollection(name));
    }

    public static void postRenameCollection(String name) {
        EventBus.getDefault().post(new RenameCollection(name));
    }

    public static void postCollectionRenamed(String name) {
        EventBus.getDefault().post(new CollectionRenamed(name));
    }

    public static void postCollectionDropped(String name) {
        EventBus.getDefault().post(new CollectionDropped(name));
    }

    public static void postConnectionAdded(long connectionId) {
        EventBus.getDefault().post(new ConnectionAdded(connectionId));
    }

    public static void postConnectionUpdated(long connectionId) {
        EventBus.getDefault().post(new ConnectionUpdated(connectionId));
    }

    public static void postEditDocument(String content) {
        EventBus.getDefault().post(new EditDocument(content));
    }

    public static void postDocumentEdited(String content) {
        EventBus.getDefault().post(new DocumentEdited(content));
    }

    public static void postDocumentCreated(String content) {
        EventBus.getDefault().post(new DocumentCreated(content));
    }

    public static void postDocumentDeleted() {
        EventBus.getDefault().post(new DocumentDeleted());
    }

    public static void postChangeDatabase(String name) {
        EventBus.getDefault().post(new ChangeDatabase(name));
    }

    public static void postQueryNamed(String name) {
        EventBus.getDefault().post(new QueryNamed(name));
    }

    public static void postQueryUpdated(String query) {
        EventBus.getDefault().post(new QueryUpdated(query));
    }

    public static class AddConnection {
    }

    public static class AddDocument {
    }

    public static class DocumentDeleted {
    }

    public static class ConnectionSelected {
        public long ConnectionId;

        public ConnectionSelected(long connectionId) {
            ConnectionId = connectionId;
        }
    }

    public static class ConnectionDeleted {
    }

    public static class Connected {
        public long ConnectionId;

        public Connected(long connectionId) {
            ConnectionId = connectionId;
        }
    }

    public static class CollectionSelected {
        public long ConnectionId;
        public String CollectionName;

        public CollectionSelected(long connectionId, String collectionName) {
            ConnectionId = connectionId;
            CollectionName = collectionName;
        }
    }

    public static class DocumentSelected {
        public String Content;

        public DocumentSelected(String content) {
            Content = content;
        }
    }

    public static class DocumentClicked {
        public String Content;

        public DocumentClicked(String content) {
            Content = content;
        }
    }

    public static class EditDocument {
        public String Content;

        public EditDocument(String content) {
            Content = content;
        }
    }

    public static class DocumentCreated {
        public String Content;

        public DocumentCreated(String content) {
            Content = content;
        }
    }

    public static class ChangeDatabase {
        public String Name;

        public ChangeDatabase(String name) {
            Name = name;
        }
    }

    public static class QueryNamed {
        public String Name;

        public QueryNamed(String name) {
            Name = name;
        }
    }

    public static class QueryUpdated {
        public String Content;

        public QueryUpdated(String content) {
            Content = content;
        }
    }

    public static class DocumentEdited {
        public String Content;

        public DocumentEdited(String content) {
            Content = content;
        }
    }

    public static class CollectionRenamed {
        public String Name;

        public CollectionRenamed(String name) {
            Name = name;
        }
    }

    public static class RenameCollection {
        public String Name;

        public RenameCollection(String name) {
            Name = name;
        }
    }

    public static class CreateCollection {
        public String Name;

        public CreateCollection(String name) {
            Name = name;
        }
    }

    public static class CollectionDropped {
        public String Name;

        public CollectionDropped(String name) {
            Name = name;
        }
    }

    public static class ConnectionAdded {
        public long ConnectionId;

        public ConnectionAdded(long connectionId) {
            ConnectionId = connectionId;
        }
    }

    public static class ConnectionUpdated {
        public long ConnectionId;

        public ConnectionUpdated(long connectionId) {
            ConnectionId = connectionId;
        }
    }

}

