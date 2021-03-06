package com.github.jhorology.bitwig.xone.k2.layer;

import static com.github.jhorology.bitwig.xone.k2.Modules.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2Control.*;
import static com.github.jhorology.bitwig.xone.k2.XoneK2LedState.*;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.Send;
import com.bitwig.extension.controller.api.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseMixerLayer extends AbstractLayer {
  private static final Logger LOG = LoggerFactory.getLogger(BaseMixerLayer.class);

  /** @param host */
  public BaseMixerLayer(ControllerHost host) {
    super(host);
  }

  @Override
  protected boolean isOverlay() {
    return false;
  }

  @Override
  protected void setup() {
    for (int ch = 0; ch < 4; ch++) {
      Track track = TRACK_BANK.getItemAt(ch);
      Parameter pan = track.pan();
      Send send0 = track.sendBank().getItemAt(0);
      Send send1 = track.sendBank().getItemAt(1);
      Send send2 = track.sendBank().getItemAt(2);
      Parameter volume = track.volume();
      // RULE: behavior of controls should be defined at use() scope.
      use(
          knob(ch, 0)
              .onRelValue(track.pan())
              .onPressed(pan::reset)
              .onReleased(GREEN)
              .onPressed(RED),
          knob(ch, 1).onAbsValue(send0).onPressed(send0::reset).onReleased(GREEN).onPressed(RED),
          knob(ch, 2).onAbsValue(send1).onPressed(send1::reset).onReleased(GREEN).onPressed(RED),
          knob(ch, 3).onAbsValue(send2).onPressed(send2::reset).onReleased(GREEN).onPressed(RED),
          fader(ch).onAbsValue(volume),
          grid(ch, 0)
              .onPressed(SHIFT::isReleased, track.arm())
              .onPressed(SHIFT::isPressed, volume::reset)
              .led(track.arm(), RED),
          grid(ch, 1).onPressed(track.solo()).led(track.solo(), YELLOW),
          grid(ch, 2).onPressed(track.mute()).led(track.mute(), YELLOW));
    }
    use(
        M.onPressed(SHIFT::isReleased, TRANSPORT.playAction())
            .onPressed(SHIFT::isPressed, TRANSPORT.restartAction())
            .led(TRANSPORT.isPlaying(), GREEN_BEAT, GREEN),
        N.onPressed(TRANSPORT.stopAction()).led(TRANSPORT.isPlaying(), YELLOW),
        O.onPressed(TRANSPORT.recordAction()).led(TRANSPORT.isArrangerRecordEnabled(), RED),
        P.onPressed(TRANSPORT.isArrangerAutomationWriteEnabled())
            .led(TRANSPORT.isArrangerAutomationWriteEnabled(), RED),
        // RULE: first base-layer only can setup common controls
        LAYER.onReleased(GREEN).onPressed(RED),
        // NAV1.onRelValue(NAV1_MODE),
        // NAV1.onRelValue(NAV2_MODE),
        SHIFT.onReleased(GREEN).onPressed(RED));

    //    PARAMETER_NAV1.addValueObserver(v -> {
    //          LOG.info("NAV1 value:{}", v);
    //    });
    // RULE: do not use Value#addValueObserver, Layer#subscribe is provided to alternatively use.
    //    subscribe(
    //        PARAMETER_NAV1,
    //        (double v) -> {
    //          LOG.info("NAV1 value:{}", v);
    //        });
    //    subscribe(
    //        PARAMETER_NAV2,
    //        (double v) -> {
    //          LOG.info("NAV2 value:{}", v);
    //        });

    // TODO SHIFT just a modifier or layer?
    // TODO LAYER pressed -> momentary latch LayerSelector
    // TODO NAV1/NAV2 pressed + rotate -> select function
    // TODO NAV1 multi functions focused scroll
    // TODO NAV2 multi functions focused transport
  }
}
