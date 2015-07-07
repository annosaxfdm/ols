package uk.ac.ebi.spot.ols.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.task.TaskExecutor;
import uk.ac.ebi.spot.ols.config.OntologyResourceConfig;
import uk.ac.ebi.spot.ols.model.Status;
import uk.ac.ebi.spot.ols.exception.FileUpdateServiceException;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.util.FileUpdater;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 *
 * This service checks if an ontology document needs updating. It sets the status of the document in
 * a repository service.
 *
 */
public class FileUpdatingService {

    private OntologyRepositoryService ontologyRepositoryService;

    private CountDownLatch latch;

    private TaskExecutor taskExecutor;

    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public FileUpdatingService(OntologyRepositoryService ontologyRepositoryService, TaskExecutor taskExecutor, CountDownLatch latch) {
        this.ontologyRepositoryService = ontologyRepositoryService;
        this.taskExecutor = taskExecutor;
        this.latch = latch;
    }

    private class FileUpdatingTask implements Runnable {

        private OntologyDocument document;
        private FileUpdater fileUpdateService;
        private boolean force = false;

        public FileUpdatingTask(OntologyDocument document, FileUpdater fileUpdateService, boolean force) {
            this.document = document;
            this.fileUpdateService = fileUpdateService;
            this.force = force;
        }

        public void run() {
            // check if document is updated
            OntologyResourceConfig config = document.getConfig();
            document.setStatus(Status.DOWNLOADING);
            document.setUpdated(new Date());
            document.setMessage("");
            ontologyRepositoryService.update(document);

            FileUpdater.FileStatus status = null;
            try {
                status = fileUpdateService.getFile(config.getNamespace(), config.getFileLocation());
                document.setLocalPath(status.getFile().getCanonicalPath());
                if (force || status.isNew()) {
                    document.setStatus(Status.TOLOAD);
                    document.setMessage("");
                }
                else {
                    document.setStatus(Status.LOADED);
                    document.setMessage("");
                }
            } catch (FileUpdateServiceException e) {
                document.setStatus(Status.FAILED);
                document.setMessage(e.getMessage());
                log.error("Error checking: " + config.getTitle(), e);
            } catch (IOException e) {
                document.setStatus(Status.FAILED);
                document.setMessage(e.getMessage());
                log.error("Can't get canonical path for: " + status.getFile().getPath(), e);
            }
            document.setUpdated(new Date());
            ontologyRepositoryService.update(document);
            latch.countDown();

        }
    }

    public void checkForUpdates(List<OntologyDocument> documents, FileUpdater fileUpdateService) {
        checkForUpdates(documents, fileUpdateService, false);
    }

    public void checkForUpdates(List<OntologyDocument> documents, FileUpdater fileUpdateService, boolean force) {
        for(OntologyDocument document : documents) {
            getLog().info("Starting file update check for " + document.getOntologyId());
            taskExecutor.execute(new FileUpdatingTask(document, fileUpdateService, force));
        }
    }
}