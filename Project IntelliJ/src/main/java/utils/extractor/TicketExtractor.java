package utils.extractor;

import lombok.Generated;
import utils.ResultSetExtractor;
import model.bean.Ticket;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementa la funzionalità che permette di estrarre i dati di un biglietto da una {@link ResultSet}
 */
@Generated
public class TicketExtractor implements ResultSetExtractor<Ticket> {
    /**
     * Implementa la funzionalità che permette di estrarre i dati di un biglietto da una {@link ResultSet}
     * @param resultSet rappresenta l'insieme delle righe SQL
     * @return il biglietto
     * @throws SQLException
     */
    @Override
    public Ticket extract(ResultSet resultSet) throws SQLException {
        Ticket ticket = new Ticket();

        ticket.setId(resultSet.getInt("ticket.id"));
        ticket.setPrice(resultSet.getDouble("ticket.price"));
        ticket.setSeat(resultSet.getInt("ticket.seat"));
        ticket.setRowLetter(resultSet.getString("ticket.rowletter").toCharArray()[0]);

        return ticket;
    }
}
