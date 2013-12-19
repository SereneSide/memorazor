package com.ell.MemoRazor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ell.MemoRazor.data.QuizAnswer;
import com.ell.MemoRazor.data.Word;
import com.ell.MemoRazor.helpers.LanguageHelper;
import com.ell.MemoRazor.helpers.WordPlaybackManager;

import java.util.ArrayList;
import java.util.Random;

public class QuizActivity extends MemoRazorActivity {
    public static final String EXTRA_QUIZ_ANSWERS = "com.ell.QUIZ_ANSWERS";

    private Random random = new Random(System.currentTimeMillis());
    private int currentStep = 1;
    private int totalSteps;
    private long currentQuestionStartTime;

    private Word currentWord;
    private ArrayList<QuizAnswer> answers;
    private ArrayList<Integer> availableIndices;
    private ArrayList<Word> allWords;
    private TextView quizWordNumber;
    private TextView quizTranslation;
    private EditText quizAnswer;
    private Button quizAccept;
    private Button quizNext;
    private TextView quizHint;
    private ImageView quizLang;
    private WordPlaybackManager playbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz);
    }

    @Override
    protected void bindControls() {
        super.bindControls();

        quizWordNumber = (TextView) findViewById(R.id.quizWordNumber);
        quizTranslation = (TextView) findViewById(R.id.quizTranslation);
        quizAnswer = (EditText) findViewById(R.id.quizAnswer);
        quizAccept = (Button) findViewById(R.id.quizNext);
        quizNext = (Button) findViewById(R.id.quizSkip);
        quizHint = (TextView) findViewById(R.id.quizHint);
        quizLang = (ImageView) findViewById(R.id.quizLang);
    }

    @Override
    protected void initialize() {
        super.initialize();

        answers = new ArrayList<QuizAnswer>();
        allWords = (ArrayList<Word>) getIntent().getSerializableExtra(WordGroupsSelectionActivity.EXTRA_SELECTED_WORDS);
        availableIndices = new ArrayList<Integer>();
        for (int i = 0; i < allWords.size(); i++) {
            availableIndices.add(i);
        }

        playbackManager = new WordPlaybackManager(getHelper(), null);

        loadNewWord();
        refreshSteps();

        addQuizAcceptHandler();
        addQuizSkipHandler();
        addQuizAnswerChanged();

        currentQuestionStartTime = System.currentTimeMillis();
    }

    private void addQuizAnswerChanged() {
        quizAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                quizAccept.setEnabled(quizAnswer.getText().length() > 0 && quizHint.getVisibility() == View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        quizAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager imm = (InputMethodManager)getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                    enterWord();
                }
                return true;
            }
        });
    }

    private void addQuizSkipHandler() {
        quizNext.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (quizHint.getVisibility() == View.INVISIBLE) {
                            playbackManager.PlayWord(currentWord, true);
                            quizHint.setTextColor(Color.RED);
                            quizHint.setText(currentWord.getName().toUpperCase());
                            quizAnswer.setEnabled(false);
                            quizNext.setText(getResources().getText(R.string.quiz_next));
                            quizAccept.setVisibility(View.GONE);
                            quizHint.setVisibility(View.VISIBLE);
                        } else {
                            quizAccept.setVisibility(View.VISIBLE);
                            quizAnswer.setEnabled(true);
                            quizHint.setVisibility(View.INVISIBLE);
                            quizNext.setText(getResources().getText(R.string.quiz_skip));
                            nextWord(quizHint.getCurrentTextColor() == Color.RED);
                        }
                    }
                });
    }

    private void enterWord() {
        if (quizHint.getVisibility() == View.INVISIBLE) {
            String answer = quizAnswer.getText().toString().trim();
            boolean correctAnswer = answer.equalsIgnoreCase(currentWord.getName());
            quizHint.setText(currentWord.getName().toUpperCase());
            playbackManager.PlayWord(currentWord, true);
            if (correctAnswer) {
                quizHint.setTextColor(Color.GREEN);
            } else {
                quizHint.setTextColor(Color.RED);
            }

            quizNext.setText(getResources().getText(R.string.quiz_next));
            quizAnswer.setEnabled(true);
            quizAccept.setVisibility(View.GONE);
            quizHint.setVisibility(View.VISIBLE);
        } else {
            quizHint.setVisibility(View.INVISIBLE);
            quizAnswer.setEnabled(false);
            quizNext.setText(getResources().getText(R.string.quiz_skip));
            quizAccept.setVisibility(View.VISIBLE);
            nextWord(false);
        }
    }

    private void addQuizAcceptHandler() {
        quizAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterWord();
            }
        });
    }

    private void nextWord(Boolean skip) {
        if (currentStep <= totalSteps) {
            QuizAnswer answer = new QuizAnswer();
            answer.setElapsedMilliseconds(System.currentTimeMillis() - currentQuestionStartTime);
            answer.setWord(currentWord);
            answer.setQuestionNumber(currentStep);
            if (skip) {
                answer.setCorrect(false);
                answer.setSkipped(true);
            } else {
                answer.setSkipped(false);
                answer.setProposedAnswer(quizAnswer.getText().toString().trim());
                answer.setCorrect(answer.getProposedAnswer().equalsIgnoreCase(currentWord.getName()));
            }
            answers.add(answer);

            if (currentStep == totalSteps) {
                Intent quizResultsIntent = new Intent(this, QuizResultsActivity.class);
                quizResultsIntent.putExtra(EXTRA_QUIZ_ANSWERS, answers);
                startActivity(quizResultsIntent);
            } else {
                currentStep++;
                loadNewWord();
                refreshSteps();

                quizAnswer.setText("");
                currentQuestionStartTime = System.currentTimeMillis();
            }
        }
    }

    private void loadNewWord() {
        int i = random.nextInt(availableIndices.size());
        int currentWordIndex = availableIndices.get(i);
        availableIndices.remove(i);

        currentWord = allWords.get(currentWordIndex);
        quizLang.setImageResource(LanguageHelper.langCodeToImage(currentWord.getLanguage()));
        quizTranslation.setSingleLine();
        quizTranslation.setSingleLine(false);
        quizTranslation.setText(currentWord.getMeaning());
    }

    private void refreshSteps() {
        totalSteps = Math.min(App.getNumQuizQuestions(), availableIndices.size() + currentStep);

        String labelText = String.format(getResources().getString(R.string.quiz_word_number),
                currentStep,
                totalSteps);
        quizWordNumber.setText(labelText);
    }
}
