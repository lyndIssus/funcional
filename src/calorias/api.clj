(ns calorias.api
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [ring.middleware.json :as middleware]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [clj-http.client :as client]))

(defonce dados-usuario (atom nil))
(defonce transacoes (atom []))

(defn resposta-json [status corpo]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string corpo)})

(defn calcular-saldo [inicio fim]
 
 )


(defn obter-calorias-alimento [nome quantidade]
  (let [url "https://world.openfoodfacts.org/cgi/search.pl"
        params {:query-params {"search_terms" nome
                               "search_simple" "1"
                               "json" "1"
                               "page_size" "1"}}
        response (client/get url params)
        body (json/parse-string (:body response) true)
        produtos (:products body)
        alimento (first produtos)
        calorias-por-100g (get-in alimento [:nutriments :energy-kcal_100g])]
    (if (and calorias-por-100g quantidade)
      
      (* (/ calorias-por-100g 100) quantidade)
      0)))




(defroutes rotas
  (POST "/usuario" req
  (let [dados (:body req)]
    (reset! dados-usuario dados)
    (resposta-json 200 {:mensagem "Dados cadastrados com sucesso."})))

  (GET "/usuario" []
  (if @dados-usuario
    (resposta-json 200 @dados-usuario)
    (resposta-json 404 {:erro "Nenhum usuário cadastrado."})))



(POST "/consumo" req
  (let [{:keys [data alimento quantidade]} (:body req)
        calorias (obter-calorias-alimento alimento quantidade)]
    (swap! transacoes conj {:data data :descricao alimento :quantidade quantidade :calorias calorias :tipo "ganho"})
    (resposta-json 200 {:mensagem "Consumo registrado." :calorias calorias})))

  (POST "/atividade" req
    )

  (GET "/extrato" [inicio fim]
    )

  (GET "/saldo" [inicio fim]
    )

  (route/not-found "Rota não encontrada"))

(def app
  (-> rotas
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main []
  (run-jetty app {:port 3000}))
