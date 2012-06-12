  For iCal, start off by opening a calendar at
 
  http://localhost:8080/users/userA/  - iCal will discover the calendar inside
  that user.
 
  For Mozilla clients (eg thunderbird) connect directory to the calendar url, eg
 
  http://localhost:8080/users/userA/calendars/cal1/


LDAP CONFIGURATION
For thunderbird, use this:
Hostname: 127.0.0.1  (or other address as required)
Base DN: cn=users, ou=people
Port Number: 8369    (default for runnign from maven)
Bind DN: userA