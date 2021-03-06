package com.ell.MemoRazor;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.ell.MemoRazor.layouts.FlowLayout;

import java.util.Random;

public class LetterShuffleFragment extends Fragment {
    private final int errorBackgroundColor = Color.rgb(255, 100, 100);
    private Button lastWrongLetterButton;
    private String userInput;
    private OnInputLetterListener onInputCorrectLetter;
    private OnInputLetterListener onInputWrongLetter;
    private boolean noErrors = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.lettershuffle, container, false);
    }

    public boolean noErrors() {
       return noErrors;
    }

    public String getUserInput() {
        return userInput;
    }

    public void setWord (final String word) {
        lastWrongLetterButton = null;
        noErrors = true;

        FlowLayout container = (FlowLayout)getView().findViewById(R.id.lettershuffle_container);
        container.removeAllViews();
        userInput = "";

        char[] letters = word.toCharArray();
        shuffleArray(letters);

        for (char c : letters) {
            Button button = createLetterButton(word, c);
            container.addView(button);
        }
    }

    private Button createLetterButton(final String word, char currentChar) {
        char currentCharUppercase = Character.toUpperCase(currentChar);

        Button button = new Button(App.getContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button buttonClicked = (Button)view;

                String letter = buttonClicked.getText().toString().toUpperCase();
                String currentInput = userInput + letter;

                if (buttonClicked != lastWrongLetterButton && lastWrongLetterButton != null) {
                    lastWrongLetterButton.getBackground().clearColorFilter();
                    lastWrongLetterButton.invalidate();
                }

                if (word.toUpperCase().startsWith(currentInput)) {
                    lastWrongLetterButton = null;
                    userInput = currentInput;
                    buttonClicked.setVisibility(View.INVISIBLE);

                    if (onInputCorrectLetter != null) {
                        onInputCorrectLetter.onInputLetter(letter, currentInput);
                    }
                } else {
                    lastWrongLetterButton = buttonClicked;
                    lastWrongLetterButton.getBackground().setColorFilter(errorBackgroundColor, PorterDuff.Mode.MULTIPLY);
                    noErrors = false;
                    playErrorSound();

                    if (onInputWrongLetter != null) {
                        onInputWrongLetter.onInputLetter(letter, currentInput);
                    }
                }
            }
        });
        button.setText(Character.toString(currentCharUppercase));
        button.setTypeface(null, Typeface.BOLD);
        button.setLayoutParams(new FlowLayout.LayoutParams(dpToPx(50), dpToPx(50)));
        return button;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = App.getContext().getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    static void playErrorSound() {
        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 200);
    }

    static void shuffleArray(char[] letters)
    {
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = letters.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);

            char a = letters[index];
            letters[index] = letters[i];
            letters[i] = a;
        }
    }

    public void setOnInputCorrectLetter(OnInputLetterListener onInputCorrectLetter) {
        this.onInputCorrectLetter = onInputCorrectLetter;
    }

    public void setOnInputWrongLetter(OnInputLetterListener onInputWrongLetter) {
        this.onInputWrongLetter = onInputWrongLetter;
    }

    public static interface OnInputLetterListener {
        void onInputLetter(String letter, String word);
    }
}
