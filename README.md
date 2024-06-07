Run server in docker: `./gradlew server:runDocker`  
Run client: `./gradlew composeApp:wasmJsBrowserRun`


Main screaper code is in `shared/src/commonMain/kotlin/screaper`
Files in that folder contains comments  
The code in shared is written on kotlin common: that means we can compile it to any other platform. Right now it 
compiles for jmv, but it can be used on wasm or js side (we can builtin it into website with all async stuff) without 
ane changes


Some kotlin specific things:  
 - No parentheses for trailing lambda https://kotlinlang.org/docs/lambdas.html#passing-trailing-lambdas  
 - `it` parameter: https://kotlinlang.org/docs/lambdas.html#it-implicit-name-of-a-single-parameter
