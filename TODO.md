
# TODO

- [x] update Database image
- [x] make the swagger-ui available at /doc
- [x] unit tests
  - [x] more tests with extension
- [x] containerization of the app
- [x] updating makefile to handle multiple instances of the app
  - [x] The two used ports should be 8081 and 8082
- [x] creating a github action to run the tests
  - [ ] github action to push images if tests are passing
- [x] refactor the code, actually solve the task
  - [x] introduce RtreeService
  - [x] Proper responses on endpoints, (with exception handler)
  - [x] The /getunique should return only the unique filenames
- [x] java doc
- [x] test it if it works on linux
- [x] (+) add optional extension parameter to the /getunique endpoint that returns only the unique files with the given extension.
  - [ ] ability filter by multiple extensions
- [x] (+) generator endpoint that creates a structure so the app can be tested against it, and return the result of the tree command.
    - [x] use different file extensions (txt, json, yaml, c)
- [x] (+) host images on docker hub
- [x] write the readme.md