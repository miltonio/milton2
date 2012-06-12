Summarising the sync process, there would be two parts:
1. When a user makes a change to an event on the client
    Update modified date on the event: note that we will not rely on absolute 
    value of mod date, only that it has changed.
    Clear the ctag for each calendar on each server in the local sync status table 
    containing this event (because that ctag no longer represents the state of 
    the local calendar)
   
2. Syncronising with server

For each server S
    For each calendar C
        If the local ctag is different to remote ctag (note that if any local event has changed local ctag will be empty) for S
            Download minimal properties for events in C on S  (ie name, uid, etag)
            For each remote event
                If uid is present use that as identifier, otherwise use name, to find local event
                remoteChanged = remote etag differs from sync status etag for this resource
                localChanged = local modified date differs from sync status modified date
                if remoteChanged and localChanged perform conflict resolution (could be manual or policy)
                otherwise, if remoteChanged download full details and bind to local event, update local sync store
                otherwise, if localChanged upload full details to server and update local sync store

Note that the above needs to cater for additions and deletes. If a remote event 
exists and there is no corresponding local event then either an event has been 
added on the server or removed locally. To work out which we look in the SyncStatus 
table - if the remote etag exists then it has been locally deleted since the last 
sync, so should be remotely deleted. Otherwise it is remotely new and should 
be downloaded.