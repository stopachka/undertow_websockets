
(ns stopachka.cel
  (:require
   [clojure.string :as clojure-string])
  (:import
   (com.google.protobuf NullValue)
   (dev.cel.common CelFunctionDecl
                   CelOverloadDecl)
   (dev.cel.extensions CelExtensions)
   (dev.cel.common.types SimpleType MapType ListType)
   (dev.cel.compiler CelCompilerFactory)
   (dev.cel.runtime CelEvaluationException)
   (dev.cel.runtime CelRuntimeFactory
                    CelRuntime$CelFunctionBinding CelFunctionOverload$Binary)))

;; ----
;; Cel

(def type-obj (MapType/create SimpleType/STRING SimpleType/DYN))

(def type-ref-return (ListType/create SimpleType/DYN))

(def ref-fn {:decl (CelFunctionDecl/newFunctionDeclaration
                     ;; (XXX) 
                     ;; Right now, `ref` is attached on all `type-obj`. 
                     ;; `ref` _only_ works on `data` though. 
                     ;; In the future it would be nice to type `data` 
                     ;; in such a way, that we attached `ref` to it.
                    "ref"
                    [(CelOverloadDecl/newMemberOverload
                      "data_ref"
                      type-ref-return
                      [type-obj SimpleType/STRING])])
             :runtime (let [impl (reify CelFunctionOverload$Binary
                                   (apply [_ {:strs [_ctx id _etype] :as _self} path-str]
                                     []))]
                        (CelRuntime$CelFunctionBinding/from
                         "data_ref"
                         java.util.Map
                         String
                         impl))})

(def custom-fns [ref-fn])
(def custom-fn-decls (map :decl custom-fns))
(def custom-fn-bindings (map :runtime custom-fns))

(def ^:private cel-compiler
  (-> (CelCompilerFactory/standardCelCompilerBuilder)
      (.addVar "data" type-obj)
      (.addVar "auth" type-obj)
      (.addVar "newData" type-obj)
      (.addFunctionDeclarations custom-fn-decls)
      (.addLibraries [(CelExtensions/bindings)])
      (.build)))

(def ^:private cel-runtime
  (-> (CelRuntimeFactory/standardCelRuntimeBuilder)
      (.addFunctionBindings custom-fn-bindings)
      (.build)))

(defn ->ast [expr-str] (.getAst (.compile cel-compiler expr-str)))
(defn ->program [ast] (.createProgram cel-runtime ast))

(defn eval-program!
  [{:keys [cel-program]} bindings]
  (.eval cel-program bindings))

(defn ->cel-map [m]
  (proxy [java.util.AbstractMap] []
    ;; If a value is not found, we must return a null value that CEL understands
    (get [k]
      (or (get m k) NullValue/NULL_VALUE))
    ;; CEL throws if a key doesn't exist. We don't want this behavior -- we'd 
    ;; rather just return null when a key is accessed. 
    ;; To get this behavior, we override `containsKey`, so we always return true 
    ;; when checking for key presence.
    (containsKey [k]
      true)
    (entrySet []
      (.entrySet (or m {})))))

(comment
  (def ast (->ast "data.creatorEmail == auth.email"))
  (def program (->program ast))
  (eval-program! {:cel-program program}
                 {"auth" (->cel-map {"email" "stepan.p@gmail.com"})
                  "data" (->cel-map {"id" #uuid "8164fb78-6fa3-4aab-8b92-80e706bae93a"
                                     "creatorEmail" "stepan.p@gmail.com"
                                     "name" "Nonfiction"
                                     "_etype" "bookshelves"})}))

