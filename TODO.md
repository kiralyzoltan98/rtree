
# TODO

- [x] update Database image
- [x] make the swagger-ui available at /doc
- [ ] unit tests
- [x] containerization of the app
- [x] updating makefile to handle multiple instances of the app
  - [x] The two used ports should be 8081 and 8082
- [ ] creating a github action to run the tests
- [ ] refactor the code, actually solve the task
  - [x] introduce RtreeService
  - [x] Proper responses on endpoints, (with exception handler)
  - [ ] The /getunique should return only the unique filenames
- [ ] java doc
- [ ] 
- [ ] 
- [ ] (+) add optional extension parameter to the /getunique endpoint that returns only the unique files with the given extension.
- [ ] (+) generator endpoint that creates a structure so the app can be tested against it, and return the result of the tree command.
    - [ ] use different file extensions (txt, json, yaml, c)
- [x] (+) host images on docker hub
- [ ] write the readme.md