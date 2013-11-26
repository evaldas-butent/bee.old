package lt.lb.webservices.exchangerates;

import static lt.lb.webservices.exchangerates.ExchangeRatesConstants.*;

import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * WebService provides official (established by Bank of Lithuania) exchange rates of the Litas
 * against Foreign Currencies.
 * 
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.4-b01 Generated source version: 2.2
 * 
 */
@WebServiceClient(name = ExchangeRatesWS.NAME, targetNamespace = ExchangeRatesWS.NAMESPACE)
public class ExchangeRatesWS
    extends Service
{

  static final String NAMESPACE = "http://webservices.lb.lt/ExchangeRates";
  static final String PORT = "ExchangeRatesSoap";
  static final String NAME = "ExchangeRates";
  static final String ACTION = "http://webservices.lb.lt/ExchangeRates";

  private static BeeLogger logger = LogUtils.getLogger(ExchangeRatesWS.class);

  public static ResponseObject getPort() {
    return getPort(WS_DEFAULT_WSDL);
  }

  public static ResponseObject getPort(String address) {
    if (BeeUtils.isEmpty(address)) {
      return ResponseObject.error("Webservice address not defined");
    }

    logger.info("Connecting to webservice:", address);

    ExchangeRatesWS exchangeRatesWS;

    try {
      exchangeRatesWS = new ExchangeRatesWS(new URL(address));
    } catch (MalformedURLException e) {
      logger.error(e);
      return ResponseObject.error(e);
    }

    ExchangeRatesSoapPort port = exchangeRatesWS.getExchangeRatesSoapPort();
    ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        exchangeRatesWS.getWSDLDocumentLocation().toString());

    return ResponseObject.response(port);
  }

  /**
   * Returns the current exchange rate (expressed in Litas per 1 currency unit) for the specified
   * currency.
   * 
   * @param address WS address
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @return SimpleRowSet result packed in ResponseObject:
   *         <table border="1">
   *         <th>ExchangeRate defined in {@link CommonsConstants#COL_EXCHANGE_RATE}</th>
   *         <tr>
   *         <td>Exchange rate in {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getCurrentExchangeRate(String address, String currency) {
    ResponseObject response = getPort(address);

    if (response.hasErrors()) {
      return response;
    }

    logger.info("GetCurrentExchangeRate:", currency);

    String result =
        ((ExchangeRatesSoapPort) response.getResponse()).getCurrentExchangeRate(currency);

    SimpleRowSet data = new SimpleRowSet(new String[] {CommonsConstants.COL_CURRENCY_RATE});
    data.addRow(new String[] {result});

    logger.info("GetCurrentExchangeRate rows: ", data.getNumberOfRows());

    return ResponseObject.response(data);
  }

  /**
   * Returns the current exchange rate (expressed in Litas per 1 currency unit) for the specified
   * currency using {@link ExchangeRatesConstants#WS_DEFAULT_WSDL} link.
   * 
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @return SimpleRowSet result packed in ResponseObject:
   *         <table border="1">
   *         <th>ExchangeRate defined in {@link CommonsConstants#COL_EXCHANGE_RATE}</th>
   *         <tr>
   *         <td>Exchange rate in {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getCurrentExchangeRate(String currency) {
    return getCurrentExchangeRate(WS_DEFAULT_WSDL, currency);
  }

  /**
   * Returns an exchange rate (expressed in Litas per 1 currency unit) for the specified currency
   * and date using {@link ExchangeRatesConstants#WS_DEFAULT_WSDL} link
   * 
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @param date specified date
   * @return <table border="1">
   *         <th>ExchangeRate defined in {@link CommonsConstants#COL_EXCHANGE_RATE}</th>
   *         <tr>
   *         <td>Exchange rate in {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getExchangeRate(String currency, JustDate date) {
    return getExchangeRate(WS_DEFAULT_WSDL, currency, date);
  }

  /**
   * Returns an exchange rate (expressed in Litas per 1 currency unit) for the specified currency
   * and date.
   * 
   * @param address WS address of WSDL
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @param date specified date
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>ExchangeRate defined in {@link CommonsConstants#COL_EXCHANGE_RATE}</th>
   *         <tr>
   *         <td>Exchange rate in {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getExchangeRate(String address, String currency, JustDate date) {
    ResponseObject response = getPort(address);

    if (response.hasErrors()) {
      return response;
    }

    logger.info("GetExchangeRate:", currency, date.toString());

    String result =
        ((ExchangeRatesSoapPort) response.getResponse()).getExchangeRate(currency, date.toString());

    SimpleRowSet data = new SimpleRowSet(new String[] {CommonsConstants.COL_CURRENCY_RATE});
    data.addRow(new String[] {result});

    logger.info("GetExchangeRate rows: ", data.getNumberOfRows());

    return ResponseObject.response(data);
  }

  /**
   * Returns a list containing exchange rates for the specified currency that are between specified
   * dates. Using default connection {@link ExchangeRatesConstants#WS_DEFAULT_WSDL} link
   * 
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @param dateLow Interval start date
   * @param dateHigh Interval end date
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>Exchange rate date defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Name of Currency defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Quantity defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Exchange rate defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Unit defined in {@link CommonsConstants#COL_CURRENCY_UNIT}</th>
   *         <tr>
   *         <td>Date in {@link String} value</td>
   *         <td>Currency value {@link String} value</td>
   *         <td>Quantity {@link String} value</td>
   *         <td>Exchange rate {@link String} value</td>
   *         <td>Unit {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getExchangeRatesByCurrency(String currency, JustDate dateLow,
      JustDate dateHigh) {
    return getExchangeRatesByCurrency(WS_DEFAULT_WSDL, currency, dateLow, dateHigh);
  }

  /**
   * Returns a list containing exchange rates for the specified currency that are between specified
   * dates.
   * 
   * @param address address of webservice
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @param dateLow Interval start date
   * @param dateHigh Interval end date
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>Exchange rate date defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Name of Currency defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Quantity defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Exchange rate defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Unit defined in {@link CommonsConstants#COL_CURRENCY_UNIT}</th>
   *         <tr>
   *         <td>Date in {@link String} value</td>
   *         <td>Currency value {@link String} value</td>
   *         <td>Quantity {@link String} value</td>
   *         <td>Exchange rate {@link String} value</td>
   *         <td>Unit {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getExchangeRatesByCurrency(String address, String currency,
      JustDate dateLow, JustDate dateHigh) {

    Assert.notEmpty(address);
    Assert.notEmpty(currency);

    ResponseObject response = getPort(address);

    if (response.hasErrors()) {
      return response;
    }

    logger.info("ExchangeRatesByCurrency:", currency, dateLow.toString(), dateHigh.toString());

    List<Object> result =
        ((ExchangeRatesSoapPort) response.getResponse()).getExchangeRatesByCurrency(currency,
            dateLow.toString(), dateHigh.toString());

    SimpleRowSet data = new SimpleRowSet(new String[] {CommonsConstants.COL_CURRENCY_RATE_DATE,
        CommonsConstants.COL_CURRENCY_NAME, CommonsConstants.COL_CURRENCY_RATE_QUANTITY,
        CommonsConstants.COL_CURRENCY_RATE, COL_CURRENCY_UNIT});

    for (Object xmlRoot : result) {

      if (xmlRoot instanceof Element) {

        Node node = ((Element) xmlRoot).getFirstChild();

        if (node.hasChildNodes()) {
          for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            NodeList itemRow = node.getChildNodes().item(i).getChildNodes();

            String[] dataValues = new String[data.getNumberOfColumns()];

            for (int j = 0; j < itemRow.getLength(); j++) {

              if (BeeUtils.same(TAG_DATE, itemRow.item(j).getLocalName())) {
                dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE_DATE)] =
                    itemRow.item(j).getTextContent().trim();
              } else if (BeeUtils.same(TAG_CURRENCY, itemRow.item(j).getLocalName())) {
                dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_NAME)] =
                    itemRow.item(j).getTextContent().trim();
              } else if (BeeUtils.same(TAG_QUANTITY, itemRow.item(j).getLocalName())) {
                dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE_QUANTITY)] =
                    itemRow.item(j).getTextContent().trim();
              } else if (BeeUtils.same(TAG_RATE, itemRow.item(j).getLocalName())) {
                dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE)] =
                    itemRow.item(j).getTextContent().trim();
              } else if (BeeUtils.same(TAG_UNIT, itemRow.item(j).getLocalName())) {
                dataValues[data.getColumnIndex(COL_CURRENCY_UNIT)] =
                    itemRow.item(j).getTextContent().trim();
              }
            }

            data.addRow(dataValues);
          }
        }
      } else {
        logger.log(LogLevel.ERROR, "Unknown webservice output type", xmlRoot.getClass().getName());
      }
    }
    logger.info("ExchangeRatesByCurrency rows: ", data.getNumberOfRows());

    return ResponseObject.response(data);
  }

  /**
   * Returns a list containing exchange rates for the specified currency that are between specified
   * dates.
   * 
   * @param address address of webservice
   * @param currency short notation of currency, e. g. "usd", "ltl", "gbp", "lvl", etc.
   * @param dateLow Interval start date
   * @param dateHigh Interval end date
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>Exchange rate date defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Name of Currency defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Quantity defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Exchange rate defined in {@link CommonsConstants#COL_EXCHANGE_RATE_DATE}</th>
   *         <th>Unit defined in {@link CommonsConstants#COL_CURRENCY_UNIT}</th>
   *         <tr>
   *         <td>Date in {@link String} value</td>
   *         <td>Currency value {@link String} value</td>
   *         <td>Quantity {@link String} value</td>
   *         <td>Exchange rate {@link String} value</td>
   *         <td>Unit {@link String} value</td>
   *         </tr>
   *         </table>
   * 
   * @deprecated Parsing XML from string value. Please use
   *             {@link ExchangeRatesWS#getExchangeRatesByCurrency(String, String, JustDate, JustDate)}
   */
  @Deprecated
  public static ResponseObject getExchangeRatesByCurrencyStr(String address, String currency,
      JustDate dateLow, JustDate dateHigh) {

    Assert.notEmpty(address);
    Assert.notEmpty(currency);
    Assert.isPositive(dateHigh.compareTo(dateLow));

    ResponseObject response = getPort(address);

    if (response.hasErrors()) {
      return response;
    }

    logger.info("ExchangeRatesByCurrency:", currency, dateLow.toString(), dateHigh.toString());

    String result =
        ((ExchangeRatesSoapPort) response.getResponse()).getExchangeRatesByCurrencyXmlString(
            currency, dateLow.toString(), dateHigh.toString());

    SimpleRowSet data = new SimpleRowSet(new String[] {CommonsConstants.COL_CURRENCY_RATE_DATE,
        CommonsConstants.COL_CURRENCY_NAME, CommonsConstants.COL_CURRENCY_RATE_QUANTITY,
        CommonsConstants.COL_CURRENCY_RATE, COL_CURRENCY_UNIT});

    Node node = null;
    logger.info("ExchangeRatesByCurrency result:", result);

    try {
      node = XmlUtils.fromString(result).getFirstChild();

      if (BeeUtils.same(node.getLocalName(), TAG_MESSAGE)) {
        return ResponseObject.error(node.getTextContent());
      }
    } catch (Exception e) {
      return ResponseObject.error(result);
    }

    if (node.hasChildNodes()) {
      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        NodeList itemRow = node.getChildNodes().item(i).getChildNodes();

        String[] dataValues = new String[data.getNumberOfColumns()];

        for (int j = 0; j < itemRow.getLength(); j++) {

          if (BeeUtils.same(TAG_DATE, itemRow.item(j).getLocalName())) {
            dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE_DATE)] =
                itemRow.item(j).getTextContent().trim();
          } else if (BeeUtils.same(TAG_CURRENCY, itemRow.item(j).getLocalName())) {
            dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_NAME)] =
                itemRow.item(j).getTextContent().trim();
          } else if (BeeUtils.same(TAG_QUANTITY, itemRow.item(j).getLocalName())) {
            dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE_QUANTITY)] =
                itemRow.item(j).getTextContent().trim();
          } else if (BeeUtils.same(TAG_RATE, itemRow.item(j).getLocalName())) {
            dataValues[data.getColumnIndex(CommonsConstants.COL_CURRENCY_RATE)] =
                itemRow.item(j).getTextContent().trim();
          } else if (BeeUtils.same(TAG_UNIT, itemRow.item(j).getLocalName())) {
            dataValues[data.getColumnIndex(COL_CURRENCY_UNIT)] =
                itemRow.item(j).getTextContent().trim();
          }
        }

        data.addRow(dataValues);
      }
    }

    logger.info("ExchangeRatesByCurrency rows: ", data.getNumberOfRows());

    return ResponseObject.response(data);
  }

  /**
   * Returns a list of currencies. Using default connection
   * {@link ExchangeRatesConstants#WS_DEFAULT_WSDL} link
   * 
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>Name defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Description defined in {@link CommonsConstants#COL_CURRENCY_DESCRIPTION}</th>
   *         <th>DescriptionEn defined in {@link CommonsConstants#COL_CURRENCY_EN_DESCRIPTION}</th>
   *         <tr>
   *         <td>Short currency name in {@link String} value</td>
   *         <td>Currency description in Lithuanian of {@link String} value</td>
   *         <td>English currency description in English of {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getListOfCurrencies() {
    return getListOfCurrencies(WS_DEFAULT_WSDL);
  }

  /**
   * Returns a list of currencies.
   * 
   * @param address address of webservice
   * @return SimplerowSet packed in ResponseObject. SimpleRowSet logical structure:
   *         <table border="1">
   *         <th>Name defined in {@link CommonsConstants#COL_CURRENCY_NAME}</th>
   *         <th>Description defined in {@link CommonsConstants#COL_CURRENCY_DESCRIPTION}</th>
   *         <th>DescriptionEn defined in {@link CommonsConstants#COL_CURRENCY_EN_DESCRIPTION}</th>
   *         <tr>
   *         <td>Short currency name in {@link String} value</td>
   *         <td>Currency description in Lithuanian of {@link String} value</td>
   *         <td>English currency description in English of {@link String} value</td>
   *         </tr>
   *         </table>
   */
  public static ResponseObject getListOfCurrencies(String address) {
    ResponseObject response = getPort(address);

    if (response.hasErrors()) {
      return response;
    }

    logger.info("getListOfCurrences");

    List<Object> result =
        ((ExchangeRatesSoapPort) response.getResponse()).getListOfCurrencies();
    SimpleRowSet data =
        new SimpleRowSet(new String[] {CommonsConstants.COL_CURRENCY_NAME,
            COL_CURRENCY_DESCRIPTION, COL_CURRENCY_EN_DESCRIPTION});

    for (Object xmlRoot : result) {
      if (xmlRoot instanceof Element) {
        logger.info(((Element) xmlRoot).getNodeName());
        NodeList currenciesNodes = ((Element) xmlRoot).getFirstChild().getChildNodes();

        for (int i = 0; i < currenciesNodes.getLength(); i++) {
          String[] dataRow = new String[data.getNumberOfColumns()];
          logger.info("Currencies nodes", currenciesNodes.item(i));
          NodeList itemNodes = ((Element) currenciesNodes.item(i)).getChildNodes();

          for (int j = 0; j < itemNodes.getLength(); j++) {
            logger.info("item nodes", itemNodes.item(j));

            if (BeeUtils.same(itemNodes.item(j).getLocalName(), TAG_CURRENCY)) {
              logger.info("item nodes, currency value", itemNodes.item(j).getTextContent());
              dataRow[data.getColumnIndex(CommonsConstants.COL_CURRENCY_NAME)] =
                  itemNodes.item(j).getTextContent().trim();
            }
          }

          data.addRow(dataRow);
        }
      } else {
        logger.log(LogLevel.ERROR, "Unknown webservice output type", xmlRoot.getClass().getName());
      }
    }

    logger.info("getListOfCurrences rows: ", data.getNumberOfRows());

    return ResponseObject.response(data);
  }

  public ExchangeRatesWS(URL wsdlLocation) {
    super(wsdlLocation, new QName(NAMESPACE, NAME));
  }

  public ExchangeRatesWS(URL wsdlLocation, QName serviceName) {
    super(wsdlLocation, serviceName);
  }

  /**
   * 
   * @return returns ExchangeRatesSoap
   */
  @WebEndpoint(name = PORT)
  public ExchangeRatesSoapPort getExchangeRatesSoapPort() {
    return super.getPort(new QName(NAMESPACE, PORT),
        ExchangeRatesSoapPort.class);
  }

  /**
   * 
   * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.
   *          Supported features not in the <code>features</code> parameter will have their default
   *          values.
   * @return returns ExchangeRatesSoap
   */
  @WebEndpoint(name = PORT)
  public ExchangeRatesSoapPort getExchangeRatesSoapPort(WebServiceFeature... features) {
    return super.getPort(new QName(NAMESPACE, PORT),
        ExchangeRatesSoapPort.class, features);
  }

}
