# BreakOut API Changelog

# WIP on performance_testing
```diff
diff --git a/performance-testing/day-one/1_baseline.json b/performance-testing/day-two/2_4_userId_and_eventId_back_json_body.json
index ceb830e..a54280e 100644
--- a/performance-testing/day-one/1_baseline.json
+++ b/performance-testing/day-two/2_4_userId_and_eventId_back_json_body.json
@@ -5,15 +5,9 @@
     "postingLocation": {
         "latitude": 37.6155551,
         "longitude": -1.06572204,
-        "date": 1465066830,
-        "id": 25945,
         "distance": 1998.0803013089562,
-        "team": "Mitose",
-        "teamId": 17,
-        "event": "BreakOut Berlin 2016",
-        "eventId": 1,
         "locationData": {
-            "ADMINISTRATIVE_AREA_LEVEL_1": "Regi\u00f3n de Murcia",
+            "ADMINISTRATIVE_AREA_LEVEL_1": "Región de Murcia",
             "POLITICAL": "Spain",
             "ROUTE": "E-22",
             "ADMINISTRATIVE_AREA_LEVEL_2": "Murcia",
@@ -22,7 +16,7 @@
             "POSTAL_CODE": "30393",
             "STREET_NUMBER": "1"
         },
-        "duringEvent": true
+        "date": 1465066830
     },
     "media": [
         {
@@ -106,17 +100,9 @@
         }
     ],
     "user": {
-        "firstname": "Sarah",
-        "lastname": "Gr\u00e4\u00dfle",
-        "gender": "",
         "id": 78,
-        "participant": {
-            "eventId": 1,
-            "eventCity": "Berlin",
-            "teamId": 17,
-            "teamName": "Mitose",
-            "tshirtSize": "m"
-        },
+        "firstname": "Sarah",
+        "lastname": "Gräßle",
         "profilePic": {
             "id": 108,
             "type": "IMAGE",
@@ -169,29 +155,21 @@
                 }
             ]
         },
-        "roles": [
-            "PARTICIPANT",
-            "SPONSOR"
-        ],
-        "blocked": false
+        "participant": {
+            "eventId": 1,
+            "teamId": 17,
+            "teamName": "Mitose"
+        }
     },
     "comments": [
         {
             "id": 523,
-            "text": "Liebes Mitose Team! Ich hoffe, ich kann euch so erreichen. Wir sind zu einem Treffen der Stiftunglife in Berlin am Samstag, 9. Juli, eingeladen. Wollt ihr mit? Vielleicht k\u00f6nnt ihr mir bei Facebook (Susanna Eder) schreiben oder Anna eine Nachricht?! Ich glaube, ihr habt damals die Nummer gespeichert... :)",
+            "text": "Liebes Mitose Team! Ich hoffe, ich kann euch so erreichen. Wir sind zu einem Treffen der Stiftunglife in Berlin am Samstag, 9. Juli, eingeladen. Wollt ihr mit? Vielleicht könnt ihr mir bei Facebook (Susanna Eder) schreiben oder Anna eine Nachricht?! Ich glaube, ihr habt damals die Nummer gespeichert... :)",
             "date": 1467738240,
             "user": {
+                "id": 184,
                 "firstname": "Susanna",
                 "lastname": "Eder",
-                "gender": "male",
-                "id": 184,
-                "participant": {
-                    "eventId": 1,
-                    "eventCity": "Berlin",
-                    "teamId": 58,
-                    "teamName": "La Caravana SusAnna",
-                    "tshirtSize": "m"
-                },
                 "profilePic": {
                     "id": 278,
                     "type": "IMAGE",
@@ -252,12 +230,7 @@
                             "type": "IMAGE"
                         }
                     ]
-                },
-                "roles": [
-                    "PARTICIPANT",
-                    "SPONSOR"
-                ],
-                "blocked": true
+                }
             }
         }
     ],
@@ -266,21 +239,8 @@
     "hashtags": [],
     "proves": {
         "id": 1122,
-        "eventId": 1,
         "status": "WITH_PROOF",
-        "teamId": 17,
-        "team": "Mitose",
-        "sponsorId": 842,
-        "userId": 981,
-        "sponsorIsHidden": false,
-        "unregisteredSponsor": null,
         "amount": 35.0,
-        "description": "Ein Selfie mit 2 unge\u00f6ffneten Berliner Luft Flaschen an eurem Zielort. Austrinken ist optional aber erw\u00fcnscht :)",
-        "contract": {
-            "id": 2958,
-            "type": "DOCUMENT",
-            "uploadToken": null,
-            "sizes": []
-        }
+        "description": "Ein Selfie mit 2 ungeöffneten Berliner Luft Flaschen an eurem Zielort. Austrinken ist optional aber erwünscht :)"
     }
 }

```

## 1.9.3

 - events have a flag `current` whether it is a currently main event

## 1.9.1

 - `challenges_with_proof_sum` and `challenges_accepted_proof_sum` combined as `challengeSum`
 - `GET /event/{eventId}/team/{teamId}/posting/` returns actual postings instead of ids, paginated via `?page={pageNumber}` default is 0, default page size is 50 (but not necessarily)

## 1.9.0

 - `linear_distance` renamed to `distance`, `actual_distance` removed
 - `donate_sum` renamed to `donateSum` (`sponsor_sum` renamed to `sponsorSum`, `challenges_with_proof_sum` renamed to `withProofSum`, `challenges_accepted_proof_sum` renamed to `acceptedProofSum`)
 - `GET /location/since/{sinceId}/` removed
 - `GET /event/{eventId}/team/{teamId}/location/since/{sinceId}/` removed
 - `GET /event/{eventId}/location/` and `GET /event/{eventId}/team/{teamId}/location/` return an aggregation of 20 locations per Team, configurable via `?perTeam={locationsPerTeam}`
 - `GET /posting/` is now paginated via `?page={pageNumber}` default is 0, default page size is 50 (but not necessarily)
 - `GET /posting/get/since/{sinceId}/` removed
 
 ## previous
 
  - no changelog available
