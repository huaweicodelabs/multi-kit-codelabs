/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.huawei.hms.urbanhomeservices.kotlin.searchbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.huawei.hms.urbanhomeservices.R;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;

/**
 * This class is used for Material UI interface
 * Used at HomeFragment and respective layout
 *
 * @author: Huawei
 * @since : 20-01-2021
 */

public class MaterialSearchBar extends FrameLayout implements View.OnClickListener,
        Animation.AnimationListener,
        View.OnFocusChangeListener, TextView.OnEditorActionListener {
    public static final int BUTTON_NAVIGATION = 2;
    public static final int BUTTON_BACK = 3;
    public static final int VIEW_VISIBLE = 1;
    public static final int VIEW_INVISIBLE = 0;
    private CardView searchBarCardView;
    private LinearLayout inputContainer;
    private ImageView navIcon;
    private ImageView menuIcon;
    private ImageView searchIcon;
    private ImageView arrowIcon;
    private ImageView clearIcon;
    private EditText searchEdit;
    private TextView placeHolder;
    private View suggestionDivider;
    private OnSearchActionListener onSearchActionListener;
    private boolean searchOpened;
    private boolean suggestionsVisible;
    private int navIconResId;
    private int searchIconRes;
    private int arrowIconRes;
    private int clearIconRes;
    private boolean speechMode;
    private int maxSuggestionCount;
    private boolean navButtonEnabled;
    private boolean roundedSearchBarEnabled;
    private int dividerColor;
    private int searchBarColor;
    private CharSequence hintText;
    private CharSequence placeholderText;
    private int textColor;
    private int hintColor;
    private int placeholderColor;
    private int navIconTint;
    private int menuIconTint;
    private int searchIconTint;
    private int arrowIconTint;
    private int clearIconTint;
    private boolean navIconTintEnabled;
    private boolean menuIconTintEnabled;
    private boolean searchIconTintEnabled;
    private boolean arrowIconTintEnabled;
    private boolean clearIconTintEnabled;
    private boolean borderlessRippleEnabled = false;
    private int textCursorColor;
    private int highlightedTextColor;

    public MaterialSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Initialize material search bar
     *
     * @param attrs ui component attributes.
     */
    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.searchbar, this);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSearchBar);
        speechMode = array.getBoolean(R.styleable.MaterialSearchBar_mt_speechMode, false);
        maxSuggestionCount = array.getInt(R.styleable.MaterialSearchBar_mt_maxSuggestionsCount, 3);
        navButtonEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_navIconEnabled, false);
        roundedSearchBarEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_roundedSearchBarEnabled, false);
        dividerColor = array.getColor(R.styleable.MaterialSearchBar_mt_dividerColor,
                ContextCompat.getColor(getContext(), R.color.searchBarDividerColor));
        searchBarColor = array.getColor(R.styleable.MaterialSearchBar_mt_searchBarColor,
                ContextCompat.getColor(getContext(), R.color.searchBarPrimaryColor));
        searchIconRes = array.getResourceId(R.styleable.MaterialSearchBar_mt_searchIconDrawable,
                R.drawable.ic_magnify_black_48dp);
        arrowIconRes = array.getResourceId(R.styleable.MaterialSearchBar_mt_backIconDrawable,
                R.drawable.ic_arrow_left_black_48dp);
        clearIconRes = array.getResourceId(R.styleable.MaterialSearchBar_mt_clearIconDrawable,
                R.drawable.ic_close_black_48dp);
        navIconTint = array.getColor(R.styleable.MaterialSearchBar_mt_navIconTint,
                ContextCompat.getColor(getContext(), R.color.searchBarNavIconTintColor));
        menuIconTint = array.getColor(R.styleable.MaterialSearchBar_mt_menuIconTint,
                ContextCompat.getColor(getContext(), R.color.searchBarMenuIconTintColor));
        searchIconTint = array.getColor(R.styleable.MaterialSearchBar_mt_searchIconTint,
                ContextCompat.getColor(getContext(), R.color.searchBarSearchIconTintColor));
        arrowIconTint = array.getColor(R.styleable.MaterialSearchBar_mt_backIconTint,
                ContextCompat.getColor(getContext(), R.color.searchBarBackIconTintColor));
        clearIconTint = array.getColor(R.styleable.MaterialSearchBar_mt_clearIconTint,
                ContextCompat.getColor(getContext(), R.color.searchBarClearIconTintColor));
        navIconTintEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_navIconUseTint, true);
        menuIconTintEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_menuIconUseTint, true);
        searchIconTintEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_searchIconUseTint, true);
        arrowIconTintEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_backIconUseTint, true);
        clearIconTintEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_clearIconUseTint, true);
        borderlessRippleEnabled = array.getBoolean(R.styleable.MaterialSearchBar_mt_borderlessRippleEnabled, false);
        hintText = array.getString(R.styleable.MaterialSearchBar_mt_hint);
        placeholderText = array.getString(R.styleable.MaterialSearchBar_mt_placeholder);
        textColor = array.getColor(R.styleable.MaterialSearchBar_mt_textColor,
                ContextCompat.getColor(getContext(), R.color.searchBarTextColor));
        hintColor = array.getColor(R.styleable.MaterialSearchBar_mt_hintColor,
                ContextCompat.getColor(getContext(), R.color.searchBarHintColor));
        placeholderColor = array.getColor(R.styleable.MaterialSearchBar_mt_placeholderColor,
                ContextCompat.getColor(getContext(), R.color.searchBarPlaceholderColor));
        textCursorColor = array.getColor(R.styleable.MaterialSearchBar_mt_textCursorTint,
                ContextCompat.getColor(getContext(), R.color.searchBarCursorColor));
        highlightedTextColor = array.getColor(R.styleable.MaterialSearchBar_mt_highlightedTextColor,
                ContextCompat.getColor(getContext(), R.color.searchBarTextHighlightColor));
        array.recycle();
        searchBarCardView = findViewById(R.id.mt_container);
        suggestionDivider = findViewById(R.id.mt_divider);
        menuIcon = findViewById(R.id.mt_menu);
        clearIcon = findViewById(R.id.mt_clear);
        searchIcon = findViewById(R.id.mt_search);
        arrowIcon = findViewById(R.id.mt_arrow);
        searchEdit = findViewById(R.id.mt_editText);
        placeHolder = findViewById(R.id.mt_placeholder);
        inputContainer = findViewById(R.id.inputContainer);
        navIcon = findViewById(R.id.mt_nav);
        findViewById(R.id.mt_clear).setOnClickListener(this);
        setOnClickListener(this);
        arrowIcon.setOnClickListener(this);
        searchIcon.setOnClickListener(this);
        searchEdit.setOnFocusChangeListener(this);
        searchEdit.setOnEditorActionListener(this);
        navIcon.setOnClickListener(this);
        postSetup();
    }

    /**
     * basic initialize ui components after setup.
     */
    private void postSetup() {
        setupTextColors();
        setupRoundedSearchBarEnabled();
        setupSearchBarColor();
        setupIcons();
        setupSearchEditText();
    }

    /**
     * Capsule shaped searchbar enabled
     * Only works on SDK V21+ due to odd behavior on lower
     */
    private void setupRoundedSearchBarEnabled() {
        if (roundedSearchBarEnabled) {
            searchBarCardView.setRadius(getResources().getDimension(R.dimen.corner_radius_rounded));
        } else {
            searchBarCardView.setRadius(getResources().getDimension(R.dimen.corner_radius_default));
        }
    }

    /**
     * Basic color setup for search bar
     */
    private void setupSearchBarColor() {
        searchBarCardView.setCardBackgroundColor(searchBarColor);
        setupDividerColor();
    }

    /**
     * set divider color for search bar
     */
    private void setupDividerColor() {
        suggestionDivider.setBackgroundColor(dividerColor);
    }

    private void setupTextColors() {
        searchEdit.setHintTextColor(hintColor);
        searchEdit.setTextColor(textColor);
        placeHolder.setTextColor(placeholderColor);
    }

    /**
     * Setup search bar editText colorings and drawables
     */
    private void setupSearchEditText() {
        setupCursorColor();
        searchEdit.setHighlightColor(highlightedTextColor);
        if (hintText != null) {
            searchEdit.setHint(hintText);
        }
        if (placeholderText != null) {
            arrowIcon.setBackground(null);
            placeHolder.setText(placeholderText);
        }
    }

    /**
     * Setup cursor color
     */
    private void setupCursorColor() {
        try {
            Field field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(searchEdit);
            field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int cursorDrawableRes = field.getInt(searchEdit);
            Drawable cursorDrawable = Objects.requireNonNull(ContextCompat.getDrawable(getContext(),
                    cursorDrawableRes)).mutate();
            cursorDrawable.setColorFilter(textCursorColor, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {cursorDrawable, cursorDrawable};
            assert editor != null;
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "NoSuchFieldException");
        } catch (IllegalAccessException e) {
            Log.d(TAG, "IllegalAccessException");
        }
    }

    /**
     * set up Icon type for speech mode and navigation icon.
     */
    private void setupIcons() {
        navIconResId = R.drawable.ic_menu_animated;
        this.navIcon.setImageResource(navIconResId);
        setNavButtonEnabled(navButtonEnabled);
        this.arrowIcon.setImageResource(arrowIconRes);
        this.clearIcon.setImageResource(clearIconRes);
        setupNavIconTint();
        setupMenuIconTint();
        setupSearchIconTint();
        setupArrowIconTint();
        setupClearIconTint();
        setupIconRippleStyle();
    }

    /**
     * setup navigation icon tint color
     */
    private void setupNavIconTint() {
        if (navIconTintEnabled) {
            navIcon.setColorFilter(navIconTint, PorterDuff.Mode.SRC_IN);
        } else {
            navIcon.clearColorFilter();
        }
    }

    /**
     * setup menu icon tint color
     */
    private void setupMenuIconTint() {
        if (menuIconTintEnabled) {
            menuIcon.setColorFilter(menuIconTint, PorterDuff.Mode.SRC_IN);
        } else {
            menuIcon.clearColorFilter();
        }
    }

    /**
     * setup menu icon tint color
     */
    private void setupSearchIconTint() {
        if (searchIconTintEnabled) {
            searchIcon.setColorFilter(searchIconTint, PorterDuff.Mode.SRC_IN);
        } else {
            searchIcon.clearColorFilter();
        }
    }

    /**
     * setup arrow icon tint color
     */
    private void setupArrowIconTint() {
        if (arrowIconTintEnabled) {
            arrowIcon.setColorFilter(arrowIconTint, PorterDuff.Mode.SRC_IN);
        } else {
            arrowIcon.clearColorFilter();
        }
    }

    /**
     * setup clear icon tint color
     */
    private void setupClearIconTint() {
        if (clearIconTintEnabled) {
            clearIcon.setColorFilter(clearIconTint, PorterDuff.Mode.SRC_IN);
        } else {
            clearIcon.clearColorFilter();
        }
    }

    /**
     * setup ripple icon style
     */
    private void setupIconRippleStyle() {
        TypedValue rippleStyle = new TypedValue();
        if (borderlessRippleEnabled) {
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless,
                    rippleStyle, true);
        } else {
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, rippleStyle, true);
        }
        navIcon.setBackgroundResource(rippleStyle.resourceId);
        searchIcon.setBackgroundResource(rippleStyle.resourceId);
        menuIcon.setBackgroundResource(rippleStyle.resourceId);
        arrowIcon.setBackgroundResource(rippleStyle.resourceId);
        clearIcon.setBackgroundResource(rippleStyle.resourceId);
    }

    /**
     * Register listener for search bar callbacks.
     *
     * @param onSearchActionListener the callback listener
     */
    public void setOnSearchActionListener(OnSearchActionListener onSearchActionListener) {
        this.onSearchActionListener = onSearchActionListener;
    }

    /**
     * Hides search input and close arrow
     */
    public void closeSearch() {
        animateNavIcon(false);
        searchOpened = false;
        Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_right);
        out.setAnimationListener(this);
        searchIcon.setVisibility(VISIBLE);
        inputContainer.startAnimation(out);
        searchIcon.startAnimation(in);
        if (placeholderText != null) {
            placeHolder.setVisibility(VISIBLE);
            placeHolder.startAnimation(in);
        }
        if (listenerExists()) {
            onSearchActionListener.onSearchStateChanged(false);
        }
    }

    /**
     * Shows search input and close arrow
     */
    public void openSearch() {
        if (isSearchOpened()) {
            onSearchActionListener.onSearchStateChanged(true);
            searchEdit.requestFocus();
            return;
        }
        animateNavIcon(true);
        searchOpened = true;
        Animation leftIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_left);
        Animation leftOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_left);
        leftIn.setAnimationListener(this);
        placeHolder.setVisibility(GONE);
        inputContainer.setVisibility(VISIBLE);
        inputContainer.startAnimation(leftIn);
        if (listenerExists()) {
            onSearchActionListener.onSearchStateChanged(true);
        }
        searchIcon.startAnimation(leftOut);
    }

    /**
     * animate navigation icon
     *
     * @param menuState boolean to check menu state
     */
    private void animateNavIcon(boolean menuState) {
        if (menuState) {
            this.navIcon.setImageResource(R.drawable.ic_menu_animated);
        } else {
            this.navIcon.setImageResource(R.drawable.ic_back_animated);
        }
        Drawable mDrawable = navIcon.getDrawable();
        if (mDrawable instanceof Animatable) {
            ((Animatable) mDrawable).start();
        }
    }

    /**
     * Check if search bar is in edit mode
     *
     * @return true if search bar is in edit mode
     */
    public boolean isSearchOpened() {
        return searchOpened;
    }

    /**
     * Set navigation drawer menu icon enabled
     *
     * @param navButtonEnabled icon enabled
     */
    public void setNavButtonEnabled(boolean navButtonEnabled) {
        this.navButtonEnabled = navButtonEnabled;
        if (navButtonEnabled) {
            navIcon.setVisibility(VISIBLE);
            navIcon.setClickable(true);
            arrowIcon.setVisibility(GONE);
        } else {
            navIcon.setVisibility(GONE);
            navIcon.setClickable(false);
            arrowIcon.setVisibility(VISIBLE);
        }
        navIcon.requestLayout();
        placeHolder.requestLayout();
        arrowIcon.requestLayout();
    }

    /**
     * Set CardView elevation
     *
     * @param elevation desired elevation
     */
    public void setCardViewElevation(int elevation) {
        CardView cardView = findViewById(R.id.mt_container);
        cardView.setCardElevation(elevation);
    }

    /**
     * Get search text
     *
     * @return String text
     */
    public String getText() {
        return searchEdit.getText().toString();
    }

    /**
     * Set search text
     *
     * @param text text
     */
    public void setText(String text) {
        searchEdit.setText(text);
    }

    private boolean listenerExists() {
        return onSearchActionListener != null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId()) {
            if (!searchOpened) {
                openSearch();
            }
        } else if (id == R.id.mt_arrow) {
            closeSearch();
        } else if (id == R.id.mt_clear) {
            searchEdit.setText("");
            int button = searchOpened ? BUTTON_BACK : BUTTON_NAVIGATION;
            if (searchOpened) {
                closeSearch();
            }
            if (listenerExists()) {
                onSearchActionListener.onButtonClicked(button);
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (!searchOpened) {
            inputContainer.setVisibility(GONE);
            searchEdit.setText("");
        } else {
            searchIcon.setVisibility(GONE);
            searchEdit.requestFocus();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (listenerExists()) {
            onSearchActionListener.onSearchConfirmed(searchEdit.getText());
        }
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.isSearchBarVisible = searchOpened ? VIEW_VISIBLE : VIEW_INVISIBLE;
        savedState.suggestionsVisible = suggestionsVisible ? VIEW_VISIBLE : VIEW_INVISIBLE;
        savedState.speechMode = speechMode ? VIEW_VISIBLE : VIEW_INVISIBLE;
        savedState.navIconResId = navIconResId;
        savedState.searchIconRes = searchIconRes;
        savedState.maxSuggestions = maxSuggestionCount;
        if (hintText != null) {
            savedState.hint = hintText.toString();
        }
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        searchOpened = savedState.isSearchBarVisible == VIEW_VISIBLE;
        suggestionsVisible = savedState.suggestionsVisible == VIEW_VISIBLE;
        if (searchOpened) {
            inputContainer.setVisibility(VISIBLE);
            placeHolder.setVisibility(GONE);
            searchIcon.setVisibility(GONE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && searchOpened) {
            closeSearch();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Interface definition for MaterialSearchBar callbacks.
     */
    public interface OnSearchActionListener {

        /**
         * Invoked when SearchBar opened or closed
         *
         * @param enabled state
         */
        void onSearchStateChanged(boolean enabled);

        /**
         * Invoked when search confirmed and "search" button is clicked on the soft keyboard
         *
         * @param text search input
         */
        void onSearchConfirmed(CharSequence text);

        /**
         * Invoked when "speech" or "navigation" buttons clicked.
         *
         * @param buttonCode {@link #BUTTON_NAVIGATION} or {@link #BUTTON_BACK} will be passed
         */
        void onButtonClicked(int buttonCode);
    }

    /**
     * Class for saving state
     */
    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private int isSearchBarVisible;
        private int suggestionsVisible;
        private int speechMode;
        private int searchIconRes;
        private int navIconResId;
        private String hint;
        private List suggestions;
        private int maxSuggestions;

        public SavedState(Parcel source) {
            super(source);
            isSearchBarVisible = source.readInt();
            suggestionsVisible = source.readInt();
            speechMode = source.readInt();
            navIconResId = source.readInt();
            searchIconRes = source.readInt();
            hint = source.readString();
            suggestions = source.readArrayList(null);
            maxSuggestions = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isSearchBarVisible);
            out.writeInt(suggestionsVisible);
            out.writeInt(speechMode);
            out.writeInt(searchIconRes);
            out.writeInt(navIconResId);
            out.writeString(hint);
            out.writeList(suggestions);
            out.writeInt(maxSuggestions);
        }
    }
}
