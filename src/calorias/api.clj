(ns calorias.api
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [clj-http.client :as client]))

(defonce dados-usuario (atom nil))
(defonce transacoes (atom []))

(defn resposta-json [status corpo]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string corpo)})

(defn calcular-saldo [inicio fim]
  (let [filtradas (filter #(let [d (:data %)]
                             (and (>= d inicio) (<= d fim)))
                          @transacoes)
        ganho (reduce + (map :calorias (filter #(= (:tipo %) "ganho") filtradas)))
        perda (reduce + (map :calorias (filter #(= (:tipo %) "perda") filtradas)))]
    (- ganho perda)))

(defn obter-calorias-alimento [nome qtd]
  ;; Exemplo com API externa (API Ninjas ou similar)
  (let [resp (client/get "https://api.api-ninjas.com/v1/nutrition"
                         {:headers {"X-Api-Key" "SUA_CHAVE_API"} ; substitua por sua chave real
                          :query-params {"query" (str qtd "g " nome)}
                          :as :json})
        calorias (get-in resp [:body 0 :calories] 0)]
    calorias))

(defroutes rotas
  (POST "/usuario" req
    (let [dados (json/parse-string (slurp (:body req)) true)]
      (reset! dados-usuario dados)
      (resposta-json 200 {:mensagem "Dados cadastrados com sucesso."})))

  (POST "/consumo" req
    (let [{:keys [data alimento quantidade]} (json/parse-string (slurp (:body req)) true)
          calorias (obter-calorias-alimento alimento quantidade)]
      (swap! transacoes conj {:data data :descricao alimento :quantidade quantidade :calorias calorias :tipo "ganho"})
      (resposta-json 200 {:mensagem "Consumo registrado." :calorias calorias})))

  (POST "/atividade" req
    (let [{:keys [data atividade minutos]} (json/parse-string (slurp (:body req)) true)
          calorias (* 10 minutos)]
      (swap! transacoes conj {:data data :descricao atividade :minutos minutos :calorias calorias :tipo "perda"})
      (resposta-json 200 {:mensagem "Atividade registrada." :calorias calorias})))

  (GET "/extrato" [inicio fim]
    (let [res (filter #(let [d (:data %)] (and (>= d inicio) (<= d fim))) @transacoes)]
      (resposta-json 200 res)))

  (GET "/saldo" [inicio fim]
    (resposta-json 200 {:saldo (calcular-saldo inicio fim)}))

  (route/not-found "Rota não encontrada"))

(def app
  (-> rotas
      middleware/wrap-json-body
      middleware/wrap-json-response))

(defn -main []
  (run-jetty app {:port 3000}))
