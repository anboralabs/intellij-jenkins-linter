package io.jenkins.jenkinsfile.runner.bootstrap.commands;

import picocli.CommandLine;

import java.io.File;

/**
 * Contains common options required for pipeline commands.
 */
public abstract class PipelineOptions {

    /**
     * The path to the Jenkinsfile.
     */
    @CommandLine.Option(names = {"-f", "--file"},
        description = "Path to Jenkinsfile or directory containing a Jenkinsfile, defaults to ./Jenkinsfile")
    public File jenkinsfile;
}
