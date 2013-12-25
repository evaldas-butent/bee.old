package com.butent.bee.shared.websocket.messages;

import com.google.common.collect.Lists;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public abstract class Message {
  
  public enum Type {
    ADMIN {
      @Override
      Message createMessage() {
        return new AdminMessage();
      }
    },
    ECHO {
      @Override
      Message createMessage() {
        return new EchoMessage();
      }
    },
    INFO {
      @Override
      Message createMessage() {
        return new InfoMessage();
      }
    },
    LOCATION {
      @Override
      Message createMessage() {
        return new LocationMessage();
      }
    },
    LOG {
      @Override
      Message createMessage() {
        return new LogMessage();
      }
    },
    PROGRESS {
      @Override
      Message createMessage() {
        return new ProgressMessage();
      }
    },
    SESSION {
      @Override
      Message createMessage() {
        return new SessionMessage();
      }
    },
    SHOW {
      @Override
      Message createMessage() {
        return new ShowMessage();
      }
    },
    USERS {
      @Override
      Message createMessage() {
        return new UsersMessage();
      }
    };
    
    abstract Message createMessage();
  }
  
  private static BeeLogger logger = LogUtils.getLogger(Message.class);
  
  public static Message decode(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    if (arr == null || arr.length != 2) {
      logger.severe("cannot decode message", s);
      return null;
    }
    
    Type messageType = Codec.unpack(Type.class, arr[0]);
    if (messageType == null) {
      logger.severe("cannot decode message type", arr[0]);
      return null;
    }
    
    Message message = messageType.createMessage();
    message.deserialize(arr[1]);
    
    return message;
  }
  
  private final Type type;

  protected Message(Type type) {
    this.type = type;
  }
  
  public String encode() {
    List<String> data = Lists.newArrayList(Codec.pack(getType()), serialize());
    return Codec.beeSerialize(data);
  }
  
  public Type getType() {
    return type;
  }

  @Override
  public abstract String toString();

  protected abstract void deserialize(String s);

  protected abstract String serialize();
  
  protected String string(Double d) {
    return (d == null) ? null : BeeUtils.toString(d, 6);
  }

  protected String string(Enum<?> e) {
    return (e == null) ? null : e.name();
  }
}
