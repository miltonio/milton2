{
    "size": 10,
    "aggs": {
        "userID": {
            "terms": {
                "field": "reqUser",
                "size": 2147483647
            },
            "aggs": {
                "hits": {
                    "filter": {
                        "bool": {
                            "must": [
                                {
                                    "or": {
                                        "filters": [
                                            {
                                                "term": {
                                                    "contentType": "text/html"
                                                }
                                            },
                                            {
                                                "term": {
                                                    "contentType": "text/html; charset=UTF-8"
                                                }
                                            }
                                        ]
                                    }
                                },
                                {
                                    "bool": {
                                        "must": {
                                            "exists": {
                                                "field": "website"
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    }
                }
            }
        }
    },
    "query": {
        "filtered": {
            "query": {
                "match_all": {
                }
            },
            "filter": {
                "and": [
                    {
                        "bool": {
                            "must": [
                                {
                                    "or": {
                                        "filters": [
                                            {
                                                "term": {
                                                    "contentType": "text/html"
                                                }
                                            },
                                            {
                                                "term": {
                                                    "contentType": "text/html; charset=UTF-8"
                                                }
                                            }
                                        ]
                                    }
                                },
                                {
                                    "term": {
                                        "reqMethod": "get"
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    }
}