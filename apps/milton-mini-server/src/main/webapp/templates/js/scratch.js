 var x = {
   "fields": ["category","message","profileId","path","branchId","appliesTo","userName","fullName","eventDate","itemTitle"],
   "size":5,
   "query":{
      "match_all":{
      }
   },
   "sort": { "eventDate": { "order": "desc" }}}