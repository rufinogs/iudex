package com.example.aplicacion.services;

import com.example.aplicacion.Entities.Problem;
import com.example.aplicacion.Entities.Result;
import com.example.aplicacion.Entities.Submission;
import com.example.aplicacion.Entities.SubmissionProblemValidator;
import com.example.aplicacion.Repository.*;
import com.example.aplicacion.rabbitMQ.RabbitResultExecutionSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

//Clase que valida que el problema introducido sea correcto. Primero ejecuta el problema y luego comprueba que los resultados son los q tienen q ser
@Service

public class ProblemValidatorService {

    Logger logger = LoggerFactory.getLogger(ProblemValidatorService.class);
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private SubmissionProblemValidatorRepository submissionProblemValidatorRepository;
    @Autowired
    private RabbitResultExecutionSender sender;

    public void validateProblem(Problem problemA) {
        Optional<Problem> problem = problemRepository.findProblemById(problemA.getId());
        logger.warn("Might cause NoSuchElementException if problem " + problemA.getId() + " is not in database");

        //Recorremos la lista de submission y las enviamos
        if (problem.get().getSubmissionProblemValidators().size() != 0) {
            for (SubmissionProblemValidator submissionProblemValidator : problem.get().getSubmissionProblemValidators()) {

                Submission submission = submissionProblemValidator.getSubmission();
                logger.info("La submision " + submission.getId() + " del problema " + problem.get().getId() + " se empieza a recorrer");

                //NO HACE FALTA CREAR LOS RESULTS AQUI> SE CREAN EN SUBMISSIONPROBLEMVALIDATORSERVICE
            /*
            List<InNOut> entradasProblemaVisible = problem.getEntradaVisible();
            List<InNOut> salidaCorrectaProblemaVisible = problem.getSalidaVisible();

            //Creamos los result correspondientes
            int numeroEntradasVisible = entradasProblemaVisible.size();
            for(int i =0; i<numeroEntradasVisible; i++){
                Result resAux = new Result(entradasProblemaVisible.get(i), submission.getCodigo(), salidaCorrectaProblemaVisible.get(i), submission.getLanguage(), submission.getFilename(), problem.getTimeout(), problem.getMemoryLimit() );
                resultRepository.save(resAux);
                submission.addResult(resAux);
            }

            //Creamos las entradas no visibles
            List<InNOut> entradasProblema = problem.getEntradaOculta();
            List<InNOut> salidaCorrectaProblema = problem.getSalidaOculta();
            int numeroEntradas = entradasProblema.size();
            for(int i =0; i<numeroEntradas; i++){
                Result resAux = new Result(entradasProblema.get(i), submission.getCodigo(), salidaCorrectaProblema.get(i), submission.getLanguage(), submission.getFilename(), problem.getTimeout(), problem.getMemoryLimit());
                resultRepository.save(resAux);
                submission.addResult(resAux);
            }


            //Guardamos la submission
            submissionProblemValidatorRepository.save(submissionProblemValidator);
             */

                //Ejecutamos
                if (submission.getLanguage() != null) {

                    for (Result res : submission.getResults()) {
                        logger.info(" El result " + res.getId() + " de la submission " + submission.getId() + " se manda a ejecutar");
                        sender.sendMessage(res);
                    }
                } else {
                    logger.error(" El lenguaje no esta soportado");

                }

            }
        }
        //Si es un problema sin submission validamos
        else {
            problem.get().setValido(true);
            logger.info("El problema " + problem.get().getNombreEjercicio() + " ha sido validado (NO TIENE CASOS DE PRUEBA)");
            problemRepository.save(problem.get());
        }

    }


    public void checkIfProblemFinishedAndDoValidateIt(SubmissionProblemValidator submissionProblemValidator) {
        long problemId = submissionProblemValidator.getSubmission().getProblema().getId();
        //Buscamos el problema en la BBDD para estar seguros de que esta actualizado
        Optional<Problem> problem = problemRepository.findById(problemId);

        logger.warn("Might cause NoSuchElementException if problem " + problemId + " is not in database");
        logger.info("COMPROBANDO PROBLEMA: La comprobacion del problema " + problem.get().getNombreEjercicio() + " se ha comenzado");

        //Buscamos todas las submssions del problema y en caso de que haya una que no este terminada lo marcamos
        Boolean estaTerminado = true;
        for (SubmissionProblemValidator submissionProblemValidator1 : problem.get().getSubmissionProblemValidators()) {
            if (submissionProblemValidator1.getSubmission().isTerminadoDeEjecutarResults()) {
            } else {  //Aun no ha terminado
                estaTerminado = false;
                break;
            }
        }


        //Si esta terminado ejecutaremos que el resultado correspondiente de cada submission es el q tiene q ser, q los accepted sean aceepted etcetc
        if (estaTerminado) {
            //En caso de que sea valido lo apuntamos
            if (checkSubmissionResultIsValide(problem.get())) {
                problem.get().setValido(true);
                logger.info("El problema " + problem.get().getNombreEjercicio() + " ha sido validado");
            } else {
                problem.get().setValido(false);
                logger.info("El problema " + problem.get().getNombreEjercicio() + " NO es valido");
            }
            problemRepository.save(problem.get());
        }

    }

    //checkea que la submission del problema den el resultado q tienen que dar
    private boolean checkSubmissionResultIsValide(Problem problem) {
        boolean salida = true;

        for (SubmissionProblemValidator submissionProblemValidator : problem.getSubmissionProblemValidators()) {
            Submission submission = submissionProblemValidator.getSubmission();

            //Obtenemos la primera linea del resultado del Submission
            String aux = submission.getResultado();
            try {
                aux = new BufferedReader(new StringReader(aux)).readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Si el resultado esperado es igual al obtenido devolvemos true si no false
            if (submissionProblemValidator.getExpectedSolution().equals(aux)) {

            } else {
                salida = false;
                logger.warn("COMPROBANDO PROBLEMA: La submission " + submissionProblemValidator.getSubmission().getId() + " NO da el resultado esperado");
                logger.warn("COMPROBANDO PROBLEMA: Se espera " + submissionProblemValidator.getExpectedSolution() + " se obtiene " + aux);

            }

        }


        return salida;
    }

    /*
    private void waitForResult(SubmissionProblemValidator submissionProblemValidator){
        Submission submission = submissionProblemValidator.getSubmission();
        while(!submission.isCorregido()){
            try {
                Thread.sleep(10000);
                submission = submissionRepository.findSubmissionById(submission.getId());
            } catch (InterruptedException e) {
            throw new RuntimeException(e);
            }
        }
    }

     */


}
