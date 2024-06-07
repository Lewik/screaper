# Run server
Run server in docker: `./gradlew server:runDocker`
## Emulator
There is a builtin emulator-server for testing purposes  
1 of 10 request will delayed for 10 seconds  
if not: request fill be delayed from 0 to 1000 milliseconds  
also, 1 of 5 request will fail with exception  

GET Request to `/emulator/<any number>` will return
```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Product (<any number>)</title>
</head>
<body>
    <h1>Product Name: Awesome Stuff</h1>
    <p>Price: $<any number>.99</p>
</body>
</html>
```

# Run Client
## Wasm clint in browser
Run client: `./gradlew composeApp:wasmJsBrowserRun`

## curl
```shell
curl 'http://0.0.0.0:8080/screaper/calculate/10' \
  -H 'Accept: application/json' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Origin: http://localhost:8081' \
  -H 'Pragma: no-cache' \
  -H 'Referer: http://localhost:8081/' \
  --data-raw '{"urls":["http://0.0.0.0:8080/emulator/(i)"],"regexPatterns":{"price ":" <h1>.*?:\\s*(.*?)</h1>","label ":" <p>.*?:\\s*(.*?)</p>"}}' \
  --insecure
```

## Node 
(idk about strict-origin-when-cross-origin)
```javascript
fetch("http://0.0.0.0:8080/screaper/calculate/10", {
  "headers": {
    "accept": "application/json",
    "cache-control": "no-cache",
    "content-type": "application/json",
    "pragma": "no-cache",
    "Referer": "http://localhost:8081/",
    "Referrer-Policy": "strict-origin-when-cross-origin"
  },
  "body": "{\"urls\":[\"http://0.0.0.0:8080/emulator/(i)\"],\"regexPatterns\":{\"price \":\" <h1>.*?:\\\\s*(.*?)</h1>\",\"label \":\" <p>.*?:\\\\s*(.*?)</p>\"}}",
  "method": "POST"
});
```

# Run cli
Run cli: `./gradlew cli:run --args="-m 2"`  
Run cli: `./gradlew cli:run -q --console=plain --args="--help"`  
Run cli: `./gradlew cli:run -q --console=plain --args="-m 2"`  
Run cli: `./gradlew cli:run -q --console=plain  --args="-m 2 -r price=<h1>.*?:\s*(.*?)</h1> -r label2=<p>.*?:\s*(.*?)</p>"`  


# Code
Main screaper code is in `shared/src/commonMain/kotlin/screaper`
Files in that folder contains comments  
The code in shared is written on kotlin common: that means we can compile it to any other platform. Right now it 
compiles for jmv, but it can be used on wasm or js side (we can builtin it into website with all async stuff) without 
ane changes


### Some kotlin specific things  
 - No parentheses for trailing lambda https://kotlinlang.org/docs/lambdas.html#passing-trailing-lambdas  
 - `it` parameter: https://kotlinlang.org/docs/lambdas.html#it-implicit-name-of-a-single-parameter

### Mongo
(need to uncomment)
`docker run -p 27017:27017 mongo:latest`
