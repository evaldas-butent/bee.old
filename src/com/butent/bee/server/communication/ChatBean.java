package com.butent.bee.server.communication;

import static com.butent.bee.shared.communication.ChatConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ChatItem;
import com.butent.bee.shared.communication.ChatRoom;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ChatBean {

  private static BeeLogger logger = LogUtils.getLogger(ChatBean.class);

  @EJB
  UserServiceBean usr;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(reqInfo.getService());

    switch (svc) {
      case Service.CREATE_CHAT:
        response = createChat(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("chat service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  public ResponseObject getChats() {
    Long userId = usr.getCurrentUserId();
    if (!DataUtils.isId(userId)) {
      String message = BeeUtils.joinWords(NameUtils.getName(this), "current user not available");
      logger.warning(message);

      return ResponseObject.warning(message);
    }

    SqlSelect chatQuery = new SqlSelect()
        .addFields(TBL_CHATS, COL_CHAT_NAME, COL_CHAT_CREATED, COL_CHAT_CREATOR)
        .addFields(TBL_CHAT_USERS, COL_CHAT, COL_CHAT_USER_REGISTERED, COL_CHAT_USER_LAST_ACCESS)
        .addFrom(TBL_CHATS)
        .addFromInner(TBL_CHAT_USERS, sys.joinTables(TBL_CHATS, TBL_CHAT_USERS, COL_CHAT))
        .setWhere(SqlUtils.equals(TBL_CHAT_USERS, COL_CHAT_USER, userId));

    SimpleRowSet chatData = qs.getData(chatQuery);
    if (DataUtils.isEmpty(chatData)) {
      logger.info("no chats found for user", userId);
      return ResponseObject.emptyResponse();
    }

    List<ChatRoom> chats = new ArrayList<>();

    for (SimpleRow row : chatData) {
      long chatId = row.getLong(COL_CHAT);
      ChatRoom chat = new ChatRoom(chatId, row.getValue(COL_CHAT_NAME));

      Long created = row.getLong(COL_CHAT_CREATED);
      if (BeeUtils.isPositive(created)) {
        chat.setCreated(created);
      }

      Long creator = row.getLong(COL_CHAT_CREATOR);
      if (DataUtils.isId(creator)) {
        chat.setCreator(creator);
      }

      Long registered = row.getLong(COL_CHAT_USER_REGISTERED);
      if (BeeUtils.isPositive(registered)) {
        chat.setRegistered(registered);
      }

      Long lastAccess = row.getLong(COL_CHAT_USER_LAST_ACCESS);
      if (BeeUtils.isPositive(lastAccess)) {
        chat.setLastAccess(lastAccess);
      }

      List<Long> users = getChatUsers(chatId);
      for (Long u : users) {
        chat.join(u);
      }

      int messageCount = countMessages(chatId);
      if (messageCount > 0) {
        chat.setMessageCount(messageCount);

        ChatItem lastMessage = getLastMessage(chatId);
        chat.setLastMessage(lastMessage);

        int unreadCount;
        if (lastMessage != null && BeeUtils.isPositive(lastAccess)
            && BeeUtils.isMore(lastAccess, lastMessage.getTime())) {

          unreadCount = 0;
        } else {
          unreadCount = countUnread(chatId, userId, lastAccess);
        }

        if (unreadCount > 0) {
          chat.setUnreadCount(unreadCount);
        }
      }

      chats.add(chat);
    }

    if (chats.size() > 1) {
      chats.sort(null);
    }

    logger.info(chats.size(), "chats found for user", userId);
    return ResponseObject.response(chats);
  }

  private ResponseObject createChat(RequestInfo reqInfo) {
    String chatName = reqInfo.getParameter(COL_CHAT_NAME);

    List<Long> users = DataUtils.parseIdList(reqInfo.getParameter(TBL_CHAT_USERS));
    if (BeeUtils.isEmpty(users)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), TBL_CHAT_USERS);
    }

    Long userId = usr.getCurrentUserId();

    if (!DataUtils.isId(userId)) {
      String message = BeeUtils.joinWords(reqInfo.getService(), "current user not available");
      logger.severe(message);

      return ResponseObject.error(message);
    }

    long created = System.currentTimeMillis();

    SqlInsert insChat = new SqlInsert(TBL_CHATS)
        .addConstant(COL_CHAT_CREATED, created)
        .addConstant(COL_CHAT_CREATOR, userId);

    if (!BeeUtils.isEmpty(chatName)) {
      insChat.addConstant(COL_CHAT_NAME, chatName);
    }

    ResponseObject chatResponse = qs.insertDataWithResponse(insChat);
    if (chatResponse.hasErrors()) {
      return chatResponse;
    }

    Long chatId = chatResponse.getResponseAsLong();

    SqlInsert insUser = new SqlInsert(TBL_CHAT_USERS)
        .addConstant(COL_CHAT, chatId)
        .addConstant(COL_CHAT_USER_REGISTERED, created)
        .addConstant(COL_CHAT_USER, userId);

    ResponseObject userResponse = qs.insertDataWithResponse(insUser);
    if (userResponse.hasErrors()) {
      return userResponse;
    }

    ChatRoom chat = new ChatRoom(chatId, chatName);
    chat.setCreated(created);
    chat.setCreator(userId);

    chat.setRegistered(created);

    for (Long u : users) {
      if (!userId.equals(u)) {
        insUser = new SqlInsert(TBL_CHAT_USERS)
            .addConstant(COL_CHAT, chatId)
            .addConstant(COL_CHAT_USER_REGISTERED, created)
            .addConstant(COL_CHAT_USER, u);

        userResponse = qs.insertDataWithResponse(insUser);
        if (userResponse.hasErrors()) {
          return userResponse;
        }

        chat.join(u);
      }
    }

    return ResponseObject.response(chat);
  }

  private int countMessages(long chatId) {
    return qs.sqlCount(TBL_CHAT_MESSAGES, SqlUtils.equals(TBL_CHAT_MESSAGES, COL_CHAT, chatId));
  }

  private int countUnread(long chatId, long userId, Long lastAccess) {
    HasConditions where = SqlUtils.and(SqlUtils.equals(TBL_CHAT_MESSAGES, COL_CHAT, chatId),
        SqlUtils.notEqual(TBL_CHAT_MESSAGES, COL_CHAT_USER, userId));

    if (BeeUtils.isPositive(lastAccess)) {
      where.add(SqlUtils.more(TBL_CHAT_MESSAGES, COL_CHAT_MESSAGE_TIME, lastAccess));
    }

    return qs.sqlCount(TBL_CHAT_MESSAGES, where);
  }

  private List<Long> getChatUsers(long chatId) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_CHAT_USERS, COL_CHAT_USER)
        .addFrom(TBL_CHAT_USERS)
        .setWhere(SqlUtils.equals(TBL_CHAT_USERS, COL_CHAT, chatId))
        .addOrder(TBL_CHAT_USERS, COL_CHAT_USER_REGISTERED, sys.getIdName(TBL_CHAT_USERS));

    return qs.getLongList(query);
  }

  private ChatItem getLastMessage(long chatId) {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_CHAT_MESSAGES, COL_CHAT_USER, COL_CHAT_MESSAGE_TIME, COL_CHAT_MESSAGE_TEXT)
        .addFrom(TBL_CHAT_MESSAGES)
        .setWhere(SqlUtils.equals(TBL_CHAT_MESSAGES, COL_CHAT, chatId))
        .addOrderDesc(TBL_CHAT_MESSAGES, COL_CHAT_MESSAGE_TIME)
        .setLimit(1);

    SimpleRowSet data = qs.getData(query);
    if (DataUtils.isEmpty(data)) {
      return null;
    }

    SimpleRow row = data.getRow(0);

    return new ChatItem(row.getLong(COL_CHAT_USER), row.getLong(COL_CHAT_MESSAGE_TIME),
        row.getValue(COL_CHAT_MESSAGE_TEXT));
  }
}
