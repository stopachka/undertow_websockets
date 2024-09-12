# stopachka/alpha-cel


```bash 
# use java-22 
java --version
# openjdk 22 2024-03-19
# OpenJDK Runtime Environment Corretto-22.0.0.36.2 (build 22+36-FR)
# OpenJDK 64-Bit Server VM Corretto-22.0.0.36.2 (build 22+36-FR, mixed mode, sharing) 

# check your `clj` version: 
clj --version
# Clojure CLI version 1.11.1.1208  

# See that if you use 1.12.0-alpha5, things work: 
clj -M:use-5:run-m 
# => 10000000

# see that if you use 1.12.0-alpha12, things break:
clj -M:use-12:run-m 
Execution error (ClassCastException) at stopachka.cel/eval-program! (cel.clj:66).
class clojure.lang.Var$Unbound cannot be cast to class java.lang.ClassLoader (clojure.lang.Var$Unbound is in unnamed module of loader 'app'; java.lang.ClassLoader is in module java.base of loader 'bootstrap')
# Detail: https://gist.github.com/stopachka/a289a3a960ce1d1423490384b3b3ef11

# open an NREPL session to dig deeper. See that if you _eval_ the form, things work
clj -M:use-12:nrepl
```
