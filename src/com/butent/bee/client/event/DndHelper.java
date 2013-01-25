package com.butent.bee.client.event;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.MotionEvent;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.State;
import com.butent.bee.shared.utils.BeeUtils;

public class DndHelper {

  public static final Predicate<Long> alwaysTarget = Predicates.alwaysTrue();

  private static String dataType = null;

  private static Long dataId = null;
  private static Long relatedId = null;

  private static String dataDescription = null;

  private static MotionEvent motionEvent = null;

  public static void fillContent(String contentType,
      Long contentId, Long relId, String contentDescription) {

    setDataType(contentType);

    setDataId(contentId);
    setRelatedId(relId);

    setDataDescription(contentDescription);
  }

  public static String getDataDescription() {
    return dataDescription;
  }

  public static Long getDataId() {
    return dataId;
  }

  public static String getDataType() {
    return dataType;
  }

  public static Long getRelatedId() {
    return relatedId;
  }

  public static boolean isTarget(String contentType) {
    return isTarget(contentType, alwaysTarget);
  }

  public static void makeSource(final DndSource widget, final String contentType,
      final Long contentId, final Long relId, final String contentDescription,
      final String dragStyle, final boolean fireMotion) {

    Assert.notNull(widget);
    Assert.notNull(contentType);
    Assert.notNull(contentId);

    DomUtils.setDraggable(widget.asWidget());

    widget.addDragStartHandler(new DragStartHandler() {
      @Override
      public void onDragStart(DragStartEvent event) {
        if (!BeeUtils.isEmpty(dragStyle)) {
          widget.asWidget().addStyleName(dragStyle);
        }

        EventUtils.allowMove(event);
        EventUtils.setDndData(event, contentId);

        fillContent(contentType, contentId, relId, contentDescription);

        if (fireMotion) {
          setMotionEvent(new MotionEvent(contentType, widget, event.getNativeEvent().getClientX(),
              event.getNativeEvent().getClientY()));
        }
      }
    });

    if (fireMotion) {
      widget.addDragHandler(new DragHandler() {
        @Override
        public void onDrag(DragEvent event) {
          if (getMotionEvent() != null) {
            int x = event.getNativeEvent().getClientX();
            int y = event.getNativeEvent().getClientY();

            if (x > 0 || y > 0) {
              getMotionEvent().moveTo(x, y);
              BeeKeeper.getBus().fireEvent(getMotionEvent());
            }
          }
        }
      });
    }

    widget.addDragEndHandler(new DragEndHandler() {
      @Override
      public void onDragEnd(DragEndEvent event) {
        if (!BeeUtils.isEmpty(dragStyle)) {
          widget.asWidget().removeStyleName(dragStyle);
        }
        reset();
      }
    });
  }

  public static void makeTarget(final DndTarget widget, final String contentType,
      final String overStyle, final Predicate<Long> targetPredicate, final Procedure<Long> onDrop) {

    Assert.notNull(widget);
    Assert.notNull(contentType);
    Assert.notNull(targetPredicate);
    Assert.notNull(onDrop);

    widget.addDragEnterHandler(new DragEnterHandler() {
      @Override
      public void onDragEnter(DragEnterEvent event) {
        if (isTarget(contentType, targetPredicate)) {
          if (widget.getTargetState() == null) {
            if (!BeeUtils.isEmpty(overStyle)) {
              widget.asWidget().addStyleName(overStyle);
            }
            widget.setTargetState(State.ACTIVATED);

          } else if (widget.getTargetState() == State.ACTIVATED) {
            widget.setTargetState(State.PENDING);
          }
        }
      }
    });

    widget.addDragOverHandler(new DragOverHandler() {
      @Override
      public void onDragOver(DragOverEvent event) {
        if (widget.getTargetState() != null) {
          EventUtils.selectDropMove(event);
        }
      }
    });

    widget.addDragLeaveHandler(new DragLeaveHandler() {
      @Override
      public void onDragLeave(DragLeaveEvent event) {
        if (widget.getTargetState() == State.ACTIVATED) {
          if (!BeeUtils.isEmpty(overStyle)) {
            widget.asWidget().removeStyleName(overStyle);
          }
          widget.setTargetState(null);

        } else if (widget.getTargetState() == State.PENDING) {
          widget.setTargetState(State.ACTIVATED);
        }
      }
    });

    widget.addDropHandler(new DropHandler() {
      @Override
      public void onDrop(DropEvent event) {
        if (widget.getTargetState() != null) {
          event.stopPropagation();
          onDrop.call(getDataId());
        }
      }
    });
  }

  public static void reset() {
    fillContent(null, null, null, null);
    setMotionEvent(null);
  }

  private static MotionEvent getMotionEvent() {
    return motionEvent;
  }

  private static boolean isTarget(String contentType, Predicate<Long> targetPredicate) {
    return BeeUtils.same(getDataType(), contentType) && targetPredicate.apply(getDataId());
  }

  private static void setDataDescription(String dataDescription) {
    DndHelper.dataDescription = dataDescription;
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

  private DndHelper() {
  }
}
