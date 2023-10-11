/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.protocol.bot.state;

import com.github.steveice10.mc.protocol.data.game.entity.Effect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.pistonmaster.serverwrecker.protocol.bot.model.EffectData;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Data
public class EntityEffectState {
    private final Map<Effect, InternalEffectState> effects = new EnumMap<>(Effect.class);

    public void updateEffect(Effect effect, int amplifier, int duration, boolean ambient, boolean showParticles) {
        effects.put(effect, new InternalEffectState(amplifier, ambient, showParticles, duration));
    }

    public void removeEffect(Effect effect) {
        effects.remove(effect);
    }

    public Optional<EffectData> getEffect(Effect effect) {
        var state = effects.get(effect);

        if (state == null) {
            return Optional.empty();
        }

        return Optional.of(new EffectData(
                effect,
                state.getAmplifier(),
                state.getDuration(),
                state.isAmbient(),
                state.isShowParticles()
        ));
    }

    public void tick() {
        effects.values().forEach(effect -> effect.setDuration(effect.getDuration() - 1));
        effects.values().removeIf(effect -> effect.getDuration() <= 0);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class InternalEffectState {
        private final int amplifier;
        private final boolean ambient;
        private final boolean showParticles;
        private int duration;
    }
}