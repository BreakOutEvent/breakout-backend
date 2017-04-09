# BreakOut API Changelog

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