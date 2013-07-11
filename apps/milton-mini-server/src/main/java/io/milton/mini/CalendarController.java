/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.mini;

import io.milton.annotations.Authenticate;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Get;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Post;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.cloud.common.CurrentDateService;
import io.milton.cloud.common.DefaultCurrentDateService;
import io.milton.cloud.common.store.FileSystemBlobStore;
import io.milton.common.ModelAndView;
import io.milton.vfs.content.DbHashStore;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.Branch;
import io.milton.vfs.db.CalEvent;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Organisation;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;

@ResourceController
public class CalendarController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CalendarController.class);

	
    public CalendarController() {

    }
       

    @ChildrenOf
    public CalendarsHome getUsersHome(Profile profile) {
        return new CalendarsHome(profile);
    }
    
    @Get
    public String showUserCalendarsHome(CalendarsHome home) {
        return "calendarsHome";
    }
    
    @ChildrenOf
    public List<Calendar> getUserCalendars(CalendarsHome calendarsHome) {
        return calendarsHome.profile.getCalendars();
    }

    @Get
    public String showUserCalendar(Calendar home) {
        return "calendar";
    }    

    public class CalendarsHome {
        private final Profile profile;

        public CalendarsHome(Profile profile) {
            this.profile = profile;
        }
        
        
        public String getName() {
            return "cals";
        }
    }

}
