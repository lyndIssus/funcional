(ns calorias.cli
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(defn enviar [rota dados]
  (client/post (str "http://localhost:3000" rota)
               {:body (json/generate-string dados)
                :headers {"Content-Type" "application/json"}
                :as :json}))

(defn ler-entrada [& prompts]
  (reduce (fn [acc p]
            (println p)
            (conj acc (read-line)))
          []
          prompts))

(defn executar-opcao [op]
  (cond
    (= op "1") (let [[altura peso idade sexo] (ler-entrada "Altura:" "Peso:" "Idade:" "Sexo:")]
                 (println (:body (enviar "/usuario"
                                         {:altura altura :peso peso :idade idade :sexo sexo}))))
    (= op "2") (let [[data alimento qtd] (ler-entrada "Data (YYYY-MM-DD):" "Alimento:" "Quantidade:")]
                 (println (:body (enviar "/consumo"
                                         {:data data :alimento alimento :quantidade (Integer/parseInt qtd)}))))
    (= op "3") (let [[data ativ dur] (ler-entrada "Data (YYYY-MM-DD):" "Atividade:" "Duração (min):")]
                 (println (:body (enviar "/atividade"
                                         {:data data :atividade ativ :minutos (Integer/parseInt dur)}))))
    (= op "4") (let [[ini fim] (ler-entrada "Início (YYYY-MM-DD):" "Fim (YYYY-MM-DD):")]
                 (println (:body (client/get "http://localhost:3000/extrato"
                                             {:query-params {"inicio" ini "fim" fim}
                                              :as :json}))))
    (= op "5") (let [[ini fim] (ler-entrada "Início (YYYY-MM-DD):" "Fim (YYYY-MM-DD):")]
                 (println (:body (client/get "http://localhost:3000/saldo"
                                             {:query-params {"inicio" ini "fim" fim}
                                              :as :json}))))
    (= op "0") (System/exit 0)
    :else (println "Opção inválida.")))

(defn menu []
  (println "\\n1. Cadastrar usuário")
  (println "2. Registrar consumo")
  (println "3. Registrar atividade")
  (println "4. Ver extrato")
  (println "5. Ver saldo")
  (println "0. Sair"))

(defn -main []
  ((fn loop-fn []
     (menu)
     (executar-opcao (read-line))
     (recur))))
