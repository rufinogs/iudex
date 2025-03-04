package com.example.aplicacion.rabbitmq;

import com.example.aplicacion.entities.Result;
import com.example.aplicacion.entities.Submission;
import com.example.aplicacion.repositories.ResultRepository;
import com.example.aplicacion.repositories.SubmissionRepository;
import com.example.aplicacion.services.ResultReviser;
import com.example.aplicacion.services.SubmissionReviserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//Clase que se encarga de recibir el result
@Service
public class RabbitResultRevieserReceiver {
    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private SubmissionReviserService submissionReviserService;


    @RabbitListener(queues = ConfigureRabbitMq.QUEUE_NAME2)
    @Transactional
    public void handleMessage(Result res) {
        //enviamos el res a revisar
        new ResultReviser().revisar(res);

        //lo guardamos
        resultRepository.save(res);

        Optional<Submission> submissionOptional = submissionRepository.findSubmissionByResults(res);
        Submission submission = submissionOptional.orElseThrow();
        submission.sumarResultCorregido();

        //en caso de que ya se hayan corregido todos mandaremos una senal para que se valide el submission
        if (submission.isTerminadoDeEjecutarResults()) {
            submissionReviserService.revisarSubmission(submission);
        }
        submissionRepository.save(submission);

    }
}
