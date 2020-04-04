package eu.dariolucia.reatmetric.persist.services;

import eu.dariolucia.reatmetric.api.common.AbstractDataItem;
import eu.dariolucia.reatmetric.api.common.LongUniqueId;
import eu.dariolucia.reatmetric.api.common.RetrievalDirection;
import eu.dariolucia.reatmetric.api.messages.IOperationalMessageArchive;
import eu.dariolucia.reatmetric.api.messages.OperationalMessage;
import eu.dariolucia.reatmetric.api.messages.OperationalMessageFilter;
import eu.dariolucia.reatmetric.api.messages.Severity;
import eu.dariolucia.reatmetric.persist.Archive;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OperationalMessageArchive extends AbstractDataItemArchive<OperationalMessage, OperationalMessageFilter> implements IOperationalMessageArchive {

    private static final Logger LOG = Logger.getLogger(OperationalMessageArchive.class.getName());

    private static final String STORE_STATEMENT = "INSERT INTO OPERATIONAL_MESSAGE_TABLE(UniqueId,GenerationTime,Id,Text,Source,Severity,AdditionalData) VALUES (?,?,?,?,?,?,?)";
    private static final String LAST_ID_QUERY = "SELECT UniqueId FROM OPERATIONAL_MESSAGE_TABLE ORDER BY UniqueId DESC FETCH FIRST ROW ONLY";
    private static final String RETRIEVE_BY_ID_QUERY = "SELECT UniqueId,GenerationTime,Id,Text,Source,Severity,AdditionalData FROM OPERATIONAL_MESSAGE_TABLE WHERE UniqueId=?";
    private static final String LAST_GENERATION_TIME_QUERY = "SELECT MAX(GenerationTime) FROM OPERATIONAL_MESSAGE_TABLE";

    public OperationalMessageArchive(Archive controller) throws SQLException {
        super(controller);
    }

    @Override
    protected void setItemPropertiesToStatement(PreparedStatement storeStatement, OperationalMessage item) throws SQLException {
        storeStatement.setLong(1, item.getInternalId().asLong());
        storeStatement.setTimestamp(2, toTimestamp(item.getGenerationTime()));
        storeStatement.setString(3, item.getId());
        storeStatement.setString(4, item.getMessage().length() > 255 ? item.getMessage().substring(0,255) : item.getMessage());
        storeStatement.setString(5, item.getSource());
        storeStatement.setShort(6, (short) item.getSeverity().ordinal());
        Object extension = item.getExtension();
        if(extension == null) {
            storeStatement.setNull(7, Types.BLOB);
        } else {
            storeStatement.setBlob(7, toInputstream(item.getExtension()));
        }
    }

    @Override
    protected PreparedStatement createStoreStatement(Connection connection) throws SQLException {
        if(LOG.isLoggable(Level.FINEST)) {
            LOG.finest(this + " - preparing store statement: " + STORE_STATEMENT);
        }
        return connection.prepareStatement(STORE_STATEMENT);
    }

    @Override
    protected String buildRetrieveByIdQuery() {
        return RETRIEVE_BY_ID_QUERY;
    }

    @Override
    protected String buildRetrieveQuery(Instant startTime, int numRecords, RetrievalDirection direction, OperationalMessageFilter filter) {
        StringBuilder query = new StringBuilder("SELECT * FROM OPERATIONAL_MESSAGE_TABLE WHERE ");
        // add time info
        if(direction == RetrievalDirection.TO_FUTURE) {
            query.append("GenerationTime >= '").append(toTimestamp(startTime).toString()).append("' ");
        } else {
            query.append("GenerationTime <= '").append(toTimestamp(startTime).toString()).append("' ");
        }
        // process filter
        if(filter != null && !filter.isClear()) {
            if(filter.getMessageTextContains() != null) {
                query.append("AND Text LIKE '%").append(filter.getMessageTextContains()).append("%' ");
            }
            if(filter.getIdList() != null && !filter.getIdList().isEmpty()) {
                query.append("AND Id IN (").append(toFilterListString(filter.getIdList(), o -> o, "'")).append(") ");
            }
            if(filter.getSourceList() != null && !filter.getSourceList().isEmpty()) {
                query.append("AND Source IN (").append(toFilterListString(filter.getSourceList(), o -> o, "'")).append(") ");
            }
            if(filter.getSeverityList() != null && !filter.getSeverityList().isEmpty()) {
                query.append("AND Severity IN (").append(toEnumFilterListString(filter.getSeverityList())).append(") ");
            }
        }
        // order by and limit
        if(direction == RetrievalDirection.TO_FUTURE) {
            query.append("ORDER BY GenerationTime ASC, UniqueId ASC FETCH NEXT ").append(numRecords).append(" ROWS ONLY");
        } else {
            query.append("ORDER BY GenerationTime DESC, UniqueId DESC FETCH NEXT ").append(numRecords).append(" ROWS ONLY");
        }
        return query.toString();
    }

    @Override
    protected OperationalMessage mapToItem(ResultSet rs, OperationalMessageFilter usedFilder) throws SQLException, IOException {
        long uniqueId = rs.getLong(1);
        Timestamp genTime = rs.getTimestamp(2);
        String messageId = rs.getString(3);
        String messageText = rs.getString(4);
        String messageSource = rs.getString(5);
        Severity severity = Severity.values()[rs.getShort(6)];
        Blob extensionBlob = rs.getBlob(7);
        Object extension = null;
        if(extensionBlob != null && !rs.wasNull()) {
            extension = toObject(extensionBlob);
        }
        return new OperationalMessage(new LongUniqueId(uniqueId), toInstant(genTime), messageId, messageText, messageSource, severity, extension);
    }

    @Override
    protected String getLastIdQuery() {
        return LAST_ID_QUERY;
    }

    @Override
    protected String getLastGenerationTimeQuery(Class<? extends AbstractDataItem> type) {
        return LAST_GENERATION_TIME_QUERY;
    }

    @Override
    protected List<String> getPurgeQuery(Instant referenceTime, RetrievalDirection direction) {
        return Arrays.asList(
                "DELETE FROM OPERATIONAL_MESSAGE_TABLE WHERE GenerationTime " + (direction == RetrievalDirection.TO_FUTURE ? ">" : "<") + "'" + toTimestamp(referenceTime) + "'"
        );
    }

    @Override
    public String toString() {
        return "Operational Message Archive";
    }

    @Override
    protected Class<OperationalMessage> getMainType() {
        return OperationalMessage.class;
    }
}
