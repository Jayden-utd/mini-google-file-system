package MetaServer;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


public class MetaData {
    private List<String> filenames=null;
    private Map<String,List<String>> fileChunks;
    private Map<String,List<Integer>> chunk_serverMapping;
    private Map<Integer,List<String>> server_chunkMapping;
    //private List<lastUpdateTS> lastUpdateTSes;
    private Map<Integer,Timestamp> lastUpdateTS;
    public MetaData(){}

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public Map<String, List<String>> getFileChunks() {
        return fileChunks;
    }

    public void setFileChunks(Map<String, List<String>> filechunks) {
        this.fileChunks = filechunks;
    }

    public Map<String, List<Integer>> getChunk_serverMapping() {
        return chunk_serverMapping;
    }

    public void setChunk_serverMapping(Map<String, List<Integer>> chunk_serverMapping) {
        this.chunk_serverMapping = chunk_serverMapping;
    }

    public Map<Integer, List<String>> getServer_chunkMapping() {
        return server_chunkMapping;
    }

    public void setServer_chunkMapping(Map<Integer, List<String>> server_chunkMapping) {
        this.server_chunkMapping = server_chunkMapping;
    }


    /*public List<lastUpdateTS> getLastUpdateTSes() {
        return lastUpdateTSes;
    }

    public void setLastUpdateTSes(List<lastUpdateTS> lastUpdateTSes) {
        this.lastUpdateTSes = lastUpdateTSes;
    }*/

    public Map<Integer, Timestamp> getLastUpdateTS() {
        return lastUpdateTS;
    }

    public void setLastUpdateTS(Map<Integer, Timestamp> lastUpdateTS) {
        this.lastUpdateTS = lastUpdateTS;
    }
}
