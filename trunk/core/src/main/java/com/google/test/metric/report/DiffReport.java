package com.google.test.metric.report;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.*;

/**
 * Represents a report on the difference between two reports, suitable for
 * rendering. Also knows how to render itself using Freemarker.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class DiffReport {
  private static final String PREFIX = "com/google/test/metric/report/";

  private Configuration cfg;
  private final Diff diff;
  private String oldSourceUrl;
  private String newSourceUrl;
  private String changelistUrl;

  public DiffReport(Diff diff) {
    this.diff = diff;
    diff.sort();
    cfg = new Configuration();
    cfg.setTemplateLoader(new ClassPathTemplateLoader(PREFIX));
  }

  public void writeHtml(Writer out) throws IOException, TemplateException {
    Template template = cfg.getTemplate("diff.html");
    template.process(this, out);
  }

  public List<Diff.ClassDiff> getClassDiffs() {
    return diff.getClassDiffs();
  }

  public Date getCurrentTime() {
    return new Date();
  }

  public String getOldSourceUrl() {
    return oldSourceUrl;
  }

  public String getNewSourceUrl() {
    return newSourceUrl;
  }

  public void setOldSourceUrl(String oldSourceUrl) {
    this.oldSourceUrl = oldSourceUrl;
  }

  public void setNewSourceUrl(String newSourceUrl) {
    this.newSourceUrl = newSourceUrl;
  }

  public String getChangelistUrl() {
    return changelistUrl;
  }

  public void setChangelistUrl(String changelistUrl) {
    this.changelistUrl = changelistUrl;
  }

}
