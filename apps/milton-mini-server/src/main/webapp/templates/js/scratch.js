function createCandidate(page, parameters, files) {
    print("createCandidate " + parameters);
    var json = '{"name": "xx"}';
    print("save json " + json);
    page.find("/jsondb/b2").createNew(parameters.get("newName"), json, "candidate");
}

function viewCandidate(page) {
    print("viewCandidate " + page.name + " - " + page.class);
    var candRes = page.find("/jsondb/b2").child(page.attributes.candId)
    var ob = candRes.jsonObject;
    print("cand name " + ob.name);
}

function searchCandidates(page, parameters) {
    var jsonSearch = "{\n" +
"\"fields\": [\"title\", \"category\",\"tags\"],\n" +
"  \"query\" : {\n" +
"    \"term\" : { \"content\" : \"farm\" }\n" +
"}\n" +
"}    \n" +
"";

    var results = page.find("/jsondb/b2").search(searchJson);
    page.attributes.searchResults = results;
    print("res " + page.attributes.searchResults);
}