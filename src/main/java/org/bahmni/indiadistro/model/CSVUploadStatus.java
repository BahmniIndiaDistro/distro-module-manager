package org.bahmni.indiadistro.model;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CSVUploadStatus {
    public CSVUploadStatus(String id, String errorFileName, String status) {
        this.id = id;
        this.errorFileName = errorFileName;
        this.status = status;
    }

    public CSVUploadStatus() {
    }

    @JsonProperty("id")
    private String id;
    @JsonProperty("errorFileName")
    private String errorFileName;
    @JsonProperty("status")
    private String status;

    public String getId() {
        return id;
    }

    public String getErrorFileName() {
        return errorFileName;
    }

    public String getStatus() {
        return status;
    }
}
