package model.dao.purchase;

import lombok.Generated;
import model.bean.Show;
import model.bean.Ticket;
import utils.extractor.FilmExtractor;
import utils.extractor.PurchaseExtractor;
import utils.ConPool;
import utils.Paginator;
import model.bean.Purchase;
import utils.extractor.ShowExtractor;
import utils.extractor.TicketExtractor;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Questa classe rappresenta il DAO di un Acquisto.
 */
@Generated
public class PurchaseDAOMethods implements PurchaseDAO {

    private ConPool conPool = ConPool.getInstance();
    private Connection con = conPool.getConnection();

    public PurchaseDAOMethods() throws SQLException {
    }

    /**
     * Implementa la funzionalità di prendere una lista degli acquisti.
     * @param paginator per gestire la paginazione
     * @return la lista degli acquisti
     * @throws SQLException
     */
    @Override
    public List<Purchase> fetchAll(Paginator paginator) throws SQLException {
        return new ArrayList<>();
    }

    /**
     * Metodo che implementa la funzionalità di restuituire la lista degli Ordini di un Account a partire da accountId
     * @param accountId identificativo numerico che rappresenta l'id dell'Account
     * @param paginator {@link Paginator}
     * @return lista degli Ordini
     * @throws SQLException
     */
    public ArrayList<Purchase> fetchAll(int accountId, Paginator paginator) throws SQLException {

        ArrayList<Purchase> purchaseList = new ArrayList<>();
        try (PreparedStatement purchaseStatement = con.prepareStatement("SELECT * FROM purchase AS pur WHERE id_customer = ? LIMIT ?,?")) {
            purchaseStatement.setInt(1, accountId);
            purchaseStatement.setInt(2, paginator.getOffset());
            purchaseStatement.setInt(3, paginator.getLimit());

            ResultSet rs = purchaseStatement.executeQuery();
            PurchaseExtractor purchaseExtractor = new PurchaseExtractor();

            while (rs.next()) {
                Purchase purchase = purchaseExtractor.extract(rs);
                purchaseList.add(purchase);
            }
            rs.close();
        }
        for(Purchase purchase : purchaseList) {
            purchase.setTicketList(new ArrayList<>());
            try (PreparedStatement ticketShowStatement = con.prepareStatement("SELECT * FROM ticket JOIN spectacle sp ON ticket.id_spectacle = sp.id WHERE ticket.id_purchase = ?")) {
                ticketShowStatement.setInt(1, purchase.getId());

                ResultSet rs = ticketShowStatement.executeQuery();
                TicketExtractor ticketExtractor = new TicketExtractor();
                ShowExtractor showExtractor = new ShowExtractor();

                while(rs.next()) {
                    Ticket ticket = ticketExtractor.extract(rs);
                    Show show = showExtractor.extract(rs);
                    ticket.setShow(show);

                    try (PreparedStatement showFilmStatement = con.prepareStatement("SELECT * FROM film JOIN spectacle sp ON sp.id_film = film.id WHERE sp.id = ?")) {
                        showFilmStatement.setInt(1, show.getId());

                        ResultSet filmResultSet = showFilmStatement.executeQuery();
                        FilmExtractor filmExtractor = new FilmExtractor();
                        if(filmResultSet.next())
                            show.setFilm(filmExtractor.extract(filmResultSet));

                        filmResultSet.close();
                    }

                    purchase.getTicketList().add(ticket);
                }
                rs.close();
            }
        }
        return purchaseList;
    }

    /**
     * Implementa la funzionalità di prendere un acquisto.
     * @param id rappresenta l'identificativo numerico dell'acquisto
     * @return l'acquisto
     * @throws SQLException
     */
    @Override
    public Purchase fetch(int id) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM purchase AS pur WHERE id = ?")) {
            ps.setInt(1,id);

            ResultSet rs = ps.executeQuery();
            PurchaseExtractor purchaseExtractor = new PurchaseExtractor();
            Purchase purchase = purchaseExtractor.extract(rs);
            rs.close();
            return purchase;
        }
    }

    /**
     * Metodo che implementa la funzionalità di aggiungere un Ordine e restituire il suo identificativo
     * @param purchase Ordine da registrare
     * @return identificativo numerico dell'Ordine aggiunto
     * @throws SQLException
     */
    public int insertAndReturnID(Purchase purchase) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("INSERT INTO purchase(date_purchase, id_customer) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(purchase.getDatePurchase()));
            ps.setInt(2, purchase.getAccount().getId());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()) {
                int purchaseId = rs.getInt(1);
                purchase.setId(purchaseId);
                return purchaseId;
            } else
                return 0;
        }
    }

    /**
     * Metodo che implementa la funzionalità di restituire quanti Ordini sono presenti nel Db
     * @return intero
     * @throws SQLException
     */
    @Override
    public int countAll() throws SQLException {

        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS ct FROM purchase")) {
            ResultSet rs = ps.executeQuery();
            int ct = 0;
            if(rs.next())
                ct = rs.getInt("ct");
            rs.close();
            return ct;
        }

    }

    /**
     * Metodo che implementa la funzionalità di restituire quanti Ordini sono presenti in un Account a partire da accountId
     * @param accountId identificativo numerico dell'Account
     * @return intero
     * @throws SQLException
     */
    public int countAll(int accountId) throws SQLException{

        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS ct FROM purchase WHERE id_customer = ?")) {
            ps.setInt(1, accountId);

            ResultSet rs = ps.executeQuery();
            int ct = 0;
            if(rs.next())
                ct = rs.getInt("ct");
            rs.close();
            return ct;
        }

    }
}
