package com.butent.bee.client.event;

import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DropEvent;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.MotionEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.State;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import elemental.js.dom.JsClipboard;
import elemental.js.dom.JsDataTransferItemList;
import elemental.js.html.JsFile;
import elemental.js.html.JsFileList;
import elemental.js.util.JsIndexable;

public final class DndHelper {

  public static final Predicate<Object> ALWAYS_TARGET = input -> true;

  private static final String TRANSFER_TYPE_FILES = "Files";

  private static String dataType;

  private static Long dataId;
  private static Long relatedId;

  private static Object data;

  private static MotionEvent motionEvent;

  private static int startX;
  private static int startY;

  public static void fillContent(String contentType, Long contentId, Long relId, Object content) {
    setDataType(contentType);

    setDataId(contentId);
    setRelatedId(relId);

    setData(content);
  }

  public static Object getData() {
    return data;
  }

  public static Long getDataId() {
    return dataId;
  }

  public static List<Property> getDataTransferInfo(DragDropEventBase<?> event) {
    Assert.notNull(event);
    List<Property> result = new ArrayList<>();

    DataTransfer dataTransfer = event.getDataTransfer();
    if (dataTransfer == null) {
      result.add(new Property("Data Transfer", "is null"));
      return result;
    }

    JsClipboard clipboard = dataTransfer.cast();

    PropertyUtils.addProperties(result,
        "Drop Effect", clipboard.getDropEffect(),
        "Effect Allowed", clipboard.getEffectAllowed());

    JsIndexable types = clipboard.getTypes();
    if (types != null) {
      int length = types.length();
      result.add(new Property("Types", BeeUtils.bracket(length)));

      for (int i = 0; i < length; i++) {
        PropertyUtils.addProperty(result, "type " + i, types.at(i));
      }
    }

    JsDataTransferItemList items = clipboard.getItems();
    if (items != null) {
      int length = items.getLength();
      result.add(new Property("Items", BeeUtils.bracket(length)));
    }

    JsFileList files = clipboard.getFiles();
    if (files != null) {
      int length = files.getLength();
      result.add(new Property("Files", BeeUtils.bracket(length)));

      for (int i = 0; i < length; i++) {
        JsFile file = files.item(i);
        if (file != null) {
          PropertyUtils.addProperty(result, "file " + i, file.getName());
        }
      }
    }

    return result;
  }

  public static String getDataType() {
    return dataType;
  }

  public static Long getRelatedId() {
    return relatedId;
  }

  public static int getStartX() {
    return startX;
  }

  public static int getStartY() {
    return startY;
  }

  public static State getTargetState(DragDropEventBase<?> event) {
    if (event != null && event.getSource() instanceof DndTarget) {
      return ((DndTarget) event.getSource()).getTargetState();
    } else {
      return null;
    }
  }

  public static boolean hasFiles(DragDropEventBase<?> event) {
    if (event == null) {
      return false;
    }

    DataTransfer dataTransfer = event.getDataTransfer();
    if (dataTransfer == null) {
      return false;
    }

    JsClipboard clipboard = dataTransfer.cast();

    JsIndexable types = clipboard.getTypes();
    if (types != null) {
      for (int i = 0; i < types.length(); i++) {
        Object type = types.at(i);

        if (type != null && BeeUtils.startsSame(type.toString(), TRANSFER_TYPE_FILES)) {
          return true;
        }
      }
    }

    JsFileList files = clipboard.getFiles();
    return files != null && files.getLength() > 0;
  }

  public static boolean isDataType(String contentType) {
    return BeeUtils.same(contentType, getDataType());
  }

  public static void makeSource(final DndSource widget, final String contentType,
      final Long contentId, final Long relId, final Object content,
      final String dragStyle, final boolean fireMotion) {

    Assert.notNull(widget);
    Assert.notNull(contentType);

    DomUtils.setDraggable(widget.asWidget());

    widget.addDragStartHandler(event -> {
      if (!BeeUtils.isEmpty(dragStyle)) {
        widget.asWidget().addStyleName(dragStyle);
      }

      EventUtils.allowCopyMove(event);
      if (contentId != null) {
        EventUtils.setDndData(event, contentId);
      } else {
        EventUtils.setDndData(event, widget.getId());
      }

      fillContent(contentType, contentId, relId, content);

      int x = event.getNativeEvent().getClientX();
      int y = event.getNativeEvent().getClientY();

      setStartX(x);
      setStartY(y);

      if (fireMotion) {
        setMotionEvent(new MotionEvent(contentType, widget, x, y));
      }
    });

    if (fireMotion) {
      widget.addDragHandler(event -> {
        if (getMotionEvent() != null) {
          int x = event.getNativeEvent().getClientX();
          int y = event.getNativeEvent().getClientY();

          if (x > 0 || y > 0) {
            getMotionEvent().moveTo(x, y);
            BeeKeeper.getBus().fireEvent(getMotionEvent());
          }
        }
      });
    }

    widget.addDragEndHandler(event -> {
      if (!BeeUtils.isEmpty(dragStyle)) {
        widget.asWidget().removeStyleName(dragStyle);
      }
      reset();
    });
  }

  public static void makeSource(DndSource widget, String contentType, Object content,
      String dragStyle) {
    makeSource(widget, contentType, null, null, content, dragStyle, false);
  }

  public static void makeTarget(final DndTarget widget, final Collection<String> contentTypes,
      final String overStyle, final Predicate<Object> targetPredicate,
      final BiConsumer<DropEvent, Object> onDrop) {

    Assert.notNull(widget);
    Assert.notEmpty(contentTypes);
    Assert.notNull(targetPredicate);
    Assert.notNull(onDrop);

    widget.addDragEnterHandler(event -> {
      if (isTarget(contentTypes, targetPredicate)) {
        if (widget.getTargetState() == null) {
          if (!BeeUtils.isEmpty(overStyle)) {
            widget.asWidget().addStyleName(overStyle);
          }
          widget.setTargetState(State.ACTIVATED);

        } else if (widget.getTargetState() == State.ACTIVATED) {
          widget.setTargetState(State.PENDING);
        }
      }
    });

    widget.addDragOverHandler(event -> {
      if (widget.getTargetState() != null) {
        if (EventUtils.hasModifierKey(event.getNativeEvent())) {
          EventUtils.selectDropCopy(event);
        } else {
          EventUtils.selectDropMove(event);
        }
      }
    });

    widget.addDragLeaveHandler(event -> {
      if (widget.getTargetState() == State.ACTIVATED) {
        if (!BeeUtils.isEmpty(overStyle)) {
          widget.asWidget().removeStyleName(overStyle);
        }
        widget.setTargetState(null);

      } else if (widget.getTargetState() == State.PENDING) {
        widget.setTargetState(State.ACTIVATED);
      }
    });

    widget.addDropHandler(event -> {
      if (widget.getTargetState() != null) {
        event.preventDefault();
        event.stopPropagation();

        onDrop.accept(event, getData());
      }
    });
  }

  public static void reset() {
    fillContent(null, null, null, null);

    setMotionEvent(null);

    setStartX(0);
    setStartY(0);
  }

  private static MotionEvent getMotionEvent() {
    return motionEvent;
  }

  private static boolean isTarget(Collection<String> contentTypes,
      Predicate<Object> targetPredicate) {

    return contentTypes.contains(getDataType()) && targetPredicate.test(getData());
  }

  private static void setData(Object data) {
    DndHelper.data = data;
  }

  private static void setDataId(Long dataId) {
    DndHelper.dataId = dataId;
  }

  private static void setDataType(String dataType) {
    DndHelper.dataType = dataType;
  }

  private static void setMotionEvent(MotionEvent motionEvent) {
    DndHelper.motionEvent = motionEvent;
  }

  private static void setRelatedId(Long relatedId) {
    DndHelper.relatedId = relatedId;
  }

  private static void setStartX(int startX) {
    DndHelper.startX = startX;
  }

  private static void setStartY(int startY) {
    DndHelper.startY = startY;
  }

  private DndHelper() {
  }
}
