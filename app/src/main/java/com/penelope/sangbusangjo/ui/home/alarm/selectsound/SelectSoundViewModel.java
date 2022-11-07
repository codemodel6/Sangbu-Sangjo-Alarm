package com.penelope.sangbusangjo.ui.home.alarm.selectsound;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SelectSoundViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private int sound;


    @Inject
    public SelectSoundViewModel(SavedStateHandle savedStateHandle) {
        Integer sound = savedStateHandle.get("sound");
        assert sound != null;
        this.sound = sound;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public int getSound() {
        return sound;
    }



    public void onSoundSelect(int sound) {
        this.sound = sound;
    }

    public void onSubmitClick() {
        event.setValue(new Event.NavigateBackWithResult(sound));
    }


    public static class Event {

        public static class NavigateBackWithResult extends Event {
            public final int sound;
            public NavigateBackWithResult(int sound) {
                this.sound = sound;
            }
        }

    }

}