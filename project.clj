
(defproject calorias "0.1.0-SNAPSHOT"
  :description "Calculadora de Calorias"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [compojure "1.6.2"]
                 [ring/ring-core "1.9.6"]
                 [ring/ring-jetty-adapter "1.9.6"]
                 [ring/ring-json "0.5.1"]
                 [cheshire "5.11.0"]
                 [clj-http "3.12.3"]]
  :main calorias.api)

