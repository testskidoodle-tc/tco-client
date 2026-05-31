/*
 * tco client — looping title screen music
 */

package meteordevelopment.meteorclient.tco;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class TcoTitleMusicSound extends AbstractSoundInstance {
    public TcoTitleMusicSound(SoundEvent sound) {
        super(sound, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = true;
        this.looping = true;
    }
}
