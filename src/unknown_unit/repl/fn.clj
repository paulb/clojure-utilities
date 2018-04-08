;; TODO Move this to experiment/fn-structure

(ns unknown-unit.repl.fn
  (:require [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :as clj.repl]
            [unknown-unit.repl.util :as util]))

;; Could this be extended to follow functions further down and show the whole path,
;; all functions removed? I.e., you get a straight sequence of exactly what happens,
;; what values are, at all stages of the operation.
;; Maybe, being ridiculously optimistic, this could even be used to interactively run code,
;; stepping through with values. Like a debugger, you know?

(def ^:private let-ops
  #{'let 'if-let 'when-let})

(defn- replacement
  [sym rep]
  (let [replace-with (get rep sym sym)]
    replace-with))
    ;; FIXME There is no way this is correct. Not a chance.
    ; (->>
      ; (if (list? replace-with) `(quote ~replace-with) replace-with)))
         ; (replace rep))))

(declare transpose-form)

(defn- bind-values
  [binding rep]
  ; (println :bind-values :rep rep)
  ; (println :bind-values :binding binding)
  (->> (partition 2 binding)
       (reduce (fn [[bind rep*] [sym bind-to]]
                 (println :binding sym :to bind-to :with rep* :acc bind)
                 (let [bind-to (if (list? bind-to)
                                 (transpose-form bind-to rep*)
                                 (replacement bind-to rep*))]
                   (println :bind-value bind-to)
                   (println :new-binding (conj bind sym bind-to))
                   (println :quoted `(quote ~bind-to))
                   (println :eval (eval `(quote ~bind-to)))
                   (println :new-rep (assoc rep* sym (eval `(quote ~bind-to))))
                   [(conj bind sym bind-to)
                    (assoc rep* sym (eval `(quote ~bind-to)))]))
               [[] rep])))

;; TODO Refactor to use lazy-seq for building the list.
;; TODO If something is rebound in a let, it needs to have it's value
;;      updated and the new value inserted from that point.
;; TODO If an input arg is a list, assume it is quoted and not to be evaluated.
;; TODO Handle :let in for macro
;; TODO Consider just evaluating all code instead of trying to figure it out.
(defn- transpose-form
  [form rep]
  ; (println :beginning :form form)
  ; (println :rep rep)
  (let [op (first form)]
    ; (println :op op)
    ; (println :letop? (let-ops op))
    ;; TODO Does not deal with lists inside let binding.
    (->> (if-let [op (let-ops op)]
           (let [next-forms (rest form)
                 [binding rep] (bind-values (first next-forms) rep)]
             [(list op (into [] (apply concat binding)))
              (rest next-forms)
              rep])
           ;; TODO If input is something which can be used as a function,
           ;;      we'd want to replace here. Not sure how best to detect.
           [(list op) (rest form) rep])
         ((fn [[acc next-forms rep]]
            ; (println :new-rep rep)
            ; (println :acc acc)
            ; (println :next-forms next-forms)
            (if-let [next-form (first next-forms)]
              (-> (if (list? next-form)
                    [(concat acc [(transpose-form next-form rep)]) (rest next-forms) rep]
                    [(concat acc [(replacement next-form rep)]) (rest next-forms) rep])
                  recur)
              (seq (remove nil? acc))))))))

(def hooboy! identity)

(defn- structure-test
  [form rep]
  (let [op (first form)]))
  ;   (println :op op)
  ;   (->> (if-let [op (let-ops op)]
  ;          (let [next-forms (rest form)]
  ;            )
  ;          []))))

(defn- interpolate-args
  [body args inputs]
  (->> (map vector args inputs)
       (into {})
       (transpose-form body)))
        ; (walk/postwalk (fn [form] (transpose-form form $)) body)))

;; Not really sure how to do this, given that we need to
;; avoid executing code we didn't write.
;; OTOH, maybe we allow people to cause destruction if they so choose.
;;       No one *has* to use this feature. Mark it as potentially dangerous.
;;       Clojure already has eval and read-string if people want to get into trouble.
;;       Also, the code they're testing presumably is being run with those inputs
;;       outside of this library, so anything that can happen here can happen
;;       there.
(defn- single-path
  "Walks the code tracking what the variables would be
  at each point, and removes conditional branchess which
  would not be followed given intended inputs."
  [body]
  ;; First we need a sequence of operations.

  ;; Then get a value from each.

  ;; On reaching a conditional form, evaluate the conditions
  ;; to determine which branch would be followed.

  ;; Reaplce the entire conditional form with the expected branch.

  ;; Repeat until the end of the function is reached.
  )

(defn- parsed-source
  [src]
  (let [[_ name & remaining] src
        next-part (first remaining)
        [docstring remaining] (if (string? next-part)
                                [next-part (rest remaining)]
                                [nil remaining])
        [args body] remaining]
    [name docstring args body]))

(defmacro structure
  "Prints the structure of a function given intended inputs.
  The inputs are expanded inline.
  Conditionals are evaluated and branches which would
  not be followed are removed."
  [function & inputs]
  (when (symbol? function)
    (let [inputs (into [] inputs)]
      `(let [fn# (edn/read-string (with-out-str (clj.repl/source ~function)))
             [name# doc# args# body#] (~parsed-source fn#)
             i-args# (->> (util/map-all (fn [arg# in#] [arg# := in#]) args# ~inputs)
                          (into []))
             i-body# (~interpolate-args body# args# ~inputs)
             new-fn# (->> (list 'defn name# doc# i-args# i-body#)
                          (remove nil?))]
         (pprint new-fn#)))))
