var a = {
    "fields":[
        "firstName","lastName","occupation","companyName","industries","hardSkills","softSkills","comments","salaryLow"
    ],
    "query":{
        "filtered":{
            "query":{
                "match_all":{}
            },
            "filter":{
                "and":{
                    "filters":[
                        {"terms":{
                                "projects":["test"]}
                        },
                        {"terms":{
                                "industry":["11 AFF Agriculture Forestry and Fishing"]
                            }}]}}}},
    "aggregations":{"genderBar":{"terms":{"field":"customText3"}},"industryBar":{"terms":{"field":"industries"}},"locationBar":{"terms":{"field":"customText20"}},"salaryBar":{"range":{"field":"salaryLow","ranges":[{"to":50000,"key":"Under 50k"},{"from":50000,"to":75000,"key":"50-75k"},{"from":75000,"to":100000,"key":"75-100k"},{"from":100000,"to":150000,"key":"100-150k"},{"from":150000,"to":200000,"key":"150-200k"},{"from":200000,"key":"Over 200k"}]}},"maxSalary":{"max":{"field":"salaryLow"}},"minSalary":{"min":{"field":"salaryLow"}
        }
    }
}