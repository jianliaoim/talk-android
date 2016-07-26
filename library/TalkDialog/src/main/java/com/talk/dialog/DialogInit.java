package com.talk.dialog;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.annotation.UiThread;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.talk.dialog.internal.MDButton;
import com.talk.dialog.internal.MDRootLayout;
import com.talk.dialog.internal.MDTintHelper;
import com.talk.dialog.progress.CircularProgressDrawable;
import com.talk.dialog.simplelist.MaterialSimpleListAdapter;
import com.talk.dialog.util.DialogUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Used by MaterialDialog while initializing the dialog. Offloads some of the code to make the main class
 * cleaner and easier to read/maintain.
 *
 * @author Aidan Follestad (afollestad)
 */
class DialogInit {

    @StyleRes
    public static int getTheme(@NonNull TalkDialog.Builder builder) {
        boolean darkTheme = DialogUtils.resolveBoolean(builder.context, R.attr.md_dark_theme, builder.theme == Theme.DARK);
        builder.theme = darkTheme ? Theme.DARK : Theme.LIGHT;
        return darkTheme ? R.style.MD_Dark : R.style.MD_Light;
    }

    @SuppressLint("NewApi")
    @UiThread
    public static void init(final TalkDialog dialog) {
        final TalkDialog.Builder builder = dialog.mBuilder;

        if (!builder.disableAnimation) {
            dialog.getWindow().getAttributes().windowAnimations = R.style.MD_Dialog_Animation;
        } else {
            dialog.getWindow().getAttributes().windowAnimations = R.style.MD_WindowAnimation;
        }
        // Set cancelable flag and dialog background color
        dialog.setCancelable(builder.cancelable);
        dialog.setCanceledOnTouchOutside(builder.cancelable);
        if (builder.backgroundColor == 0)
            builder.backgroundColor = DialogUtils.resolveColor(builder.context, R.attr.md_background_color);
        if (builder.backgroundColor != 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(builder.context.getResources().getDimension(R.dimen.md_bg_corner_radius));
            drawable.setColor(builder.backgroundColor);
            DialogUtils.setBackgroundCompat(dialog.view, drawable);
        }

        // Retrieve action button colors from theme attributes or the Builder
        if (!builder.positiveColorSet)
            builder.positiveColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_positive_color, builder.positiveColor);
        if (!builder.neutralColorSet)
            builder.neutralColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_neutral_color, builder.neutralColor);
        if (!builder.negativeColorSet)
            builder.negativeColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_negative_color, builder.negativeColor);
        if (!builder.widgetColorSet)
            builder.widgetColor = DialogUtils.resolveColor(builder.context, R.attr.md_widget_color, builder.widgetColor);

        // Retrieve default title/content colors
        if (!builder.titleColorSet) {
            final int titleColorFallback = DialogUtils.resolveColor(dialog.getContext(), android.R.attr.textColorPrimary);
            builder.titleColor = DialogUtils.resolveColor(builder.context, R.attr.md_title_color, titleColorFallback);
        }
        if (!builder.contentColorSet) {
            final int contentColorFallback = DialogUtils.resolveColor(dialog.getContext(), android.R.attr.textColorSecondary);
            builder.contentColor = DialogUtils.resolveColor(builder.context, R.attr.md_content_color, contentColorFallback);
        }
        if (!builder.itemColorSet)
            builder.itemColor = DialogUtils.resolveColor(builder.context, R.attr.md_item_color, builder.contentColor);

        // Retrieve references to views
        dialog.title = (TextView) dialog.view.findViewById(R.id.title);
        dialog.icon = (ImageView) dialog.view.findViewById(R.id.icon);
        dialog.titleFrame = dialog.view.findViewById(R.id.titleFrame);
        dialog.content = (TextView) dialog.view.findViewById(R.id.content);
        dialog.listView = (ListView) dialog.view.findViewById(R.id.contentListView);

        // Button views initially used by checkIfStackingNeeded()
        dialog.positiveButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultPositive);
        dialog.neutralButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultNeutral);
        dialog.negativeButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultNegative);

        // Don't allow the submit button to not be shown for input dialogs
        if (builder.inputCallback != null && builder.positiveText == null)
            builder.positiveText = builder.context.getText(android.R.string.ok);

        // Set up the initial visibility of action buttons based on whether or not text was set
        dialog.positiveButton.setVisibility(builder.positiveText != null ? View.VISIBLE : View.GONE);
        dialog.neutralButton.setVisibility(builder.neutralText != null ? View.VISIBLE : View.GONE);
        dialog.negativeButton.setVisibility(builder.negativeText != null ? View.VISIBLE : View.GONE);

        // Setup icon
        if (builder.icon != null) {
            dialog.icon.setVisibility(View.VISIBLE);
            dialog.icon.setImageDrawable(builder.icon);
        } else {
            Drawable d = DialogUtils.resolveDrawable(builder.context, R.attr.md_icon);
            if (d != null) {
                dialog.icon.setVisibility(View.VISIBLE);
                dialog.icon.setImageDrawable(d);
            } else {
                dialog.icon.setVisibility(View.GONE);
            }
        }

        // Setup icon size limiting
        int maxIconSize = builder.maxIconSize;
        if (maxIconSize == -1)
            maxIconSize = DialogUtils.resolveDimension(builder.context, R.attr.md_icon_max_size);
        if (builder.limitIconToDefaultSize || DialogUtils.resolveBoolean(builder.context, R.attr.md_icon_limit_icon_to_default_size))
            maxIconSize = builder.context.getResources().getDimensionPixelSize(R.dimen.md_icon_max_size);
        if (maxIconSize > -1) {
            dialog.icon.setAdjustViewBounds(true);
            dialog.icon.setMaxHeight(maxIconSize);
            dialog.icon.setMaxWidth(maxIconSize);
            dialog.icon.requestLayout();
        }

        // Setup divider color in case content scrolls
        if (!builder.dividerColorSet) {
            final int dividerFallback = DialogUtils.resolveColor(dialog.getContext(), R.attr.md_divider);
            builder.dividerColor = DialogUtils.resolveColor(builder.context, R.attr.md_divider_color, dividerFallback);
        }
        dialog.view.setDividerColor(builder.dividerColor);

        // Setup title and title frame
        if (dialog.title != null) {
//            dialog.setTypeface(dialog.title, builder.mediumFont);
            dialog.title.setTextColor(builder.titleColor);
            dialog.title.setGravity(builder.titleGravity.getGravityInt());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                //noinspection ResourceType
//                dialog.title.setTextAlignment(builder.titleGravity.getTextAlignment());
//            }

            if (builder.title == null) {
                dialog.titleFrame.setVisibility(View.GONE);
            } else {
                dialog.title.setText(builder.title);
                dialog.titleFrame.setVisibility(View.VISIBLE);
            }

            if (builder.titleTextSize != -1) {
                dialog.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, builder.titleTextSize);
            }
        }

        if (builder.titleBackgroundColor != -1) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(builder.titleBackgroundColor);
            float radius;
            if (builder.radius == -1) {
                radius = builder.context.getResources().getDimension(R.dimen.md_bg_corner_radius);
            } else {
                radius = builder.radius;
            }

            float[] radii = {radius, radius, radius, radius, 0, 0, 0, 0};
            drawable.setCornerRadii(radii);
            DialogUtils.setBackgroundCompat(dialog.titleFrame, drawable);
        }

        // Setup content
        if (dialog.content != null) {
            dialog.content.setMovementMethod(new LinkMovementMethod());
            dialog.setTypeface(dialog.content, builder.regularFont);
            dialog.content.setLineSpacing(0f, builder.contentLineSpacingMultiplier);
            if (builder.positiveColor == null)
                dialog.content.setLinkTextColor(DialogUtils.resolveColor(dialog.getContext(), android.R.attr.textColorPrimary));
            else
                dialog.content.setLinkTextColor(builder.positiveColor);
            dialog.content.setTextColor(builder.contentColor);
            dialog.content.setGravity(builder.contentGravity.getGravityInt());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                //noinspection ResourceType
                dialog.content.setTextAlignment(builder.contentGravity.getTextAlignment());
            }

            if (builder.contentTextSize != -1) {
                dialog.content.setTextSize(TypedValue.COMPLEX_UNIT_SP, builder.contentTextSize);
            }

            if (builder.content != null) {
                dialog.content.setText(builder.content);
                dialog.content.setVisibility(View.VISIBLE);
            } else {
                dialog.content.setVisibility(View.GONE);
            }
        }

        // Setup action buttons
        dialog.view.setButtonGravity(builder.buttonsGravity);
        dialog.view.setButtonStackedGravity(builder.btnStackedGravity);
        dialog.view.setForceStack(builder.forceStacking);
        boolean textAllCaps;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            textAllCaps = DialogUtils.resolveBoolean(builder.context, android.R.attr.textAllCaps, true);
            if (textAllCaps)
                textAllCaps = DialogUtils.resolveBoolean(builder.context, R.attr.textAllCaps, true);
        } else {
            textAllCaps = DialogUtils.resolveBoolean(builder.context, R.attr.textAllCaps, true);
        }

        MDButton positiveTextView = dialog.positiveButton;
        dialog.setTypeface(positiveTextView, builder.mediumFont);
        positiveTextView.setAllCapsCompat(textAllCaps);
        positiveTextView.setText(builder.positiveText);
        positiveTextView.setTextColor(builder.positiveColor);
        dialog.positiveButton.setStackedSelector(dialog.getButtonSelector(DialogAction.POSITIVE, true));
        dialog.positiveButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.POSITIVE, false));
        dialog.positiveButton.setTag(DialogAction.POSITIVE);
        dialog.positiveButton.setOnClickListener(dialog);
        dialog.positiveButton.setVisibility(View.VISIBLE);

        MDButton negativeTextView = dialog.negativeButton;
        dialog.setTypeface(negativeTextView, builder.mediumFont);
        negativeTextView.setAllCapsCompat(textAllCaps);
        negativeTextView.setText(builder.negativeText);
        negativeTextView.setTextColor(builder.negativeColor);
        dialog.negativeButton.setStackedSelector(dialog.getButtonSelector(DialogAction.NEGATIVE, true));
        dialog.negativeButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.NEGATIVE, false));
        dialog.negativeButton.setTag(DialogAction.NEGATIVE);
        dialog.negativeButton.setOnClickListener(dialog);
        dialog.negativeButton.setVisibility(View.VISIBLE);

        MDButton neutralTextView = dialog.neutralButton;
        dialog.setTypeface(neutralTextView, builder.mediumFont);
        neutralTextView.setAllCapsCompat(textAllCaps);
        neutralTextView.setText(builder.neutralText);
        neutralTextView.setTextColor(builder.neutralColor);
        dialog.neutralButton.setStackedSelector(dialog.getButtonSelector(DialogAction.NEUTRAL, true));
        dialog.neutralButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.NEUTRAL, false));
        dialog.neutralButton.setTag(DialogAction.NEUTRAL);
        dialog.neutralButton.setOnClickListener(dialog);
        dialog.neutralButton.setVisibility(View.VISIBLE);

        // Setup list dialog stuff
        if (builder.listCallbackMultiChoice != null)
            dialog.selectedIndicesList = new ArrayList<>();
        if (dialog.listView != null && (builder.items != null && builder.items.length > 0 || builder.adapter != null)) {
            dialog.listView.setSelector(dialog.getListSelector());

            // No custom adapter specified, setup the list with a MaterialDialogAdapter.
            // Which supports regular lists and single/multi choice dialogs.
            if (builder.adapter == null) {
                // Determine list type
                if (builder.listCallbackSingleChoice != null) {
                    dialog.listType = TalkDialog.ListType.SINGLE;
                } else if (builder.listCallbackMultiChoice != null) {
                    dialog.listType = TalkDialog.ListType.MULTI;
                    if (builder.selectedIndices != null)
                        dialog.selectedIndicesList = new ArrayList<>(Arrays.asList(builder.selectedIndices));
                } else {
                    dialog.listType = TalkDialog.ListType.REGULAR;
                }
                builder.adapter = new MaterialDialogAdapter(dialog,
                        TalkDialog.ListType.getLayoutForType(dialog.listType));
            } else if (builder.adapter instanceof MaterialSimpleListAdapter) {
                // Notify simple list adapter of the dialog it belongs to
                ((MaterialSimpleListAdapter) builder.adapter).setDialog(dialog, false);
            }
        }

        // Setup progress dialog stuff if needed
        setupProgressDialog(dialog);

        // Setup input dialog stuff if needed
        setupInputDialog(dialog);

        // Setup custom views
        if (builder.customView != null) {
            ((MDRootLayout) dialog.view.findViewById(R.id.root)).noTitleNoPadding();
            LinearLayout frame = (LinearLayout) dialog.view.findViewById(R.id.customViewFrame);
            dialog.customViewFrame = frame;
            View innerView = builder.customView;
            if (builder.wrapCustomViewInScroll) {
                /* Apply the frame padding to the content, this allows the ScrollView to draw it's
                   over scroll glow without clipping */
                final Resources r = dialog.getContext().getResources();
                final int framePadding = r.getDimensionPixelSize(R.dimen.md_dialog_frame_margin);
                final ScrollView sv = new ScrollView(dialog.getContext());
                int paddingTop = r.getDimensionPixelSize(R.dimen.md_content_padding_top);
                int paddingBottom = r.getDimensionPixelSize(R.dimen.md_content_padding_bottom);
                sv.setClipToPadding(false);
                if (innerView instanceof EditText) {
                    // Setting padding to an EditText causes visual errors, set it to the parent instead
                    sv.setPadding(framePadding, paddingTop, framePadding, paddingBottom);
                } else {
                    // Setting padding to scroll view pushes the scroll bars out, don't do it if not necessary (like above)
                    sv.setPadding(0, paddingTop, 0, paddingBottom);
                    innerView.setPadding(framePadding, 0, framePadding, 0);
                }
                sv.addView(innerView, new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                innerView = sv;
            }
            frame.addView(innerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Setup user listeners
        if (builder.showListener != null)
            dialog.setOnShowListener(builder.showListener);
        if (builder.cancelListener != null)
            dialog.setOnCancelListener(builder.cancelListener);
        if (builder.dismissListener != null)
            dialog.setOnDismissListener(builder.dismissListener);
        if (builder.keyListener != null)
            dialog.setOnKeyListener(builder.keyListener);

        // Setup internal show listener
        dialog.setOnShowListenerInternal();

        // Other internal initialization
        dialog.invalidateList();
        dialog.setViewInternal(dialog.view);
        dialog.checkIfListInitScroll();
    }

    @LayoutRes
    public static int getInflateLayout(TalkDialog.Builder builder) {
        if (builder.customView != null) {
            return R.layout.md_dialog_custom;
        } else if (builder.items != null && builder.items.length > 0 || builder.adapter != null) {
            return R.layout.md_dialog_list;
        } else if (builder.progress > -2) {
            return R.layout.md_dialog_progress;
        } else if (builder.indeterminateProgress) {
            if (builder.indeterminateIsHorizontalProgress)
                return R.layout.md_dialog_progress_indeterminate_horizontal;
            return R.layout.md_dialog_progress_indeterminate;
        } else if (builder.inputCallback != null) {
            return R.layout.md_dialog_input;
        } else {
            return R.layout.md_dialog_basic;
        }
    }

    public static void buildResource(TalkDialog dialog) {
        TalkDialog.Builder builder = dialog.mBuilder;
        if (dialog.title != null && !TextUtils.isEmpty(builder.title)) {
            dialog.title.setText(builder.title);
        }

        if (dialog.title != null && builder.titleColor != -1) {
            dialog.title.setTextColor(builder.titleColor);
        }

        if (dialog.content != null && !TextUtils.isEmpty(builder.content)) {
            dialog.content.setText(builder.content);
        }

        if (dialog.content != null && builder.contentColor != -1) {
            dialog.content.setTextColor(builder.contentColor);
        }

        if (dialog.titleFrame != null && builder.titleBackgroundColor != -1) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(builder.titleBackgroundColor);
            float radius;
            if (builder.radius == -1) {
                radius = builder.context.getResources().getDimension(R.dimen.md_bg_corner_radius);
            } else {
                radius = builder.radius;
            }

            float[] radii = {radius, radius, radius, radius, 0, 0, 0, 0};
            drawable.setCornerRadii(radii);
            DialogUtils.setBackgroundCompat(dialog.titleFrame, drawable);
        }

        if (builder.positiveColor != null) {
            dialog.positiveButton.setTextColor(builder.positiveColor);
        }

        if (builder.neutralColor != null) {
            dialog.negativeButton.setTextColor(builder.negativeColor);
        }
    }

    public static void buildErrorResource(TalkDialog dialog) {
        TalkDialog.Builder builder = dialog.mBuilder;
        if (dialog.title != null && !TextUtils.isEmpty(builder.titleError)) {
            dialog.title.setText(builder.titleError);
        }

        if (dialog.title != null && builder.titleErrorColor != -1) {
            dialog.title.setTextColor(builder.titleErrorColor);
        }

        if (dialog.content != null && !TextUtils.isEmpty(builder.contentError)) {
            dialog.content.setText(builder.contentError);
        }

        if (dialog.content != null && builder.contentErrorColor != -1) {
            dialog.content.setTextColor(builder.contentErrorColor);
        }

        if (dialog.titleFrame != null && builder.titleBackgroundErrorColor != -1) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(builder.titleBackgroundErrorColor);
            float radius;
            if (builder.radius == -1) {
                radius = builder.context.getResources().getDimension(R.dimen.md_bg_corner_radius);
            } else {
                radius = builder.radius;
            }

            float[] radii = {radius, radius, radius, radius, 0, 0, 0, 0};
            drawable.setCornerRadii(radii);
            DialogUtils.setBackgroundCompat(dialog.titleFrame, drawable);
        }
    }

    private static void setupProgressDialog(final TalkDialog dialog) {
        final TalkDialog.Builder builder = dialog.mBuilder;
        if (builder.indeterminateProgress || builder.progress > -2) {
            dialog.mProgress = (ProgressBar) dialog.view.findViewById(android.R.id.progress);
            if (dialog.mProgress == null) return;

            if (builder.indeterminateProgress && !builder.indeterminateIsHorizontalProgress &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                dialog.mProgress.setIndeterminateDrawable(new CircularProgressDrawable(
                        builder.widgetColor, builder.context.getResources().getDimension(R.dimen.circular_progress_border)));
                MDTintHelper.setTint(dialog.mProgress, builder.widgetColor, true);
            } else {
                MDTintHelper.setTint(dialog.mProgress, builder.widgetColor);
            }

            if (!builder.indeterminateProgress || builder.indeterminateIsHorizontalProgress) {
                dialog.mProgress.setIndeterminate(builder.indeterminateIsHorizontalProgress);
                dialog.mProgress.setProgress(0);
                dialog.mProgress.setMax(builder.progressMax);
                dialog.mProgressLabel = (TextView) dialog.view.findViewById(R.id.label);
                if (dialog.mProgressLabel != null) {
                    dialog.mProgressLabel.setTextColor(builder.contentColor);
                    dialog.setTypeface(dialog.mProgressLabel, builder.mediumFont);
                    dialog.mProgressLabel.setText(builder.progressPercentFormat.format(0));
                }
                dialog.mProgressMinMax = (TextView) dialog.view.findViewById(R.id.minMax);
                if (dialog.mProgressMinMax != null) {
                    dialog.mProgressMinMax.setTextColor(builder.contentColor);
                    dialog.setTypeface(dialog.mProgressMinMax, builder.regularFont);

                    if (builder.showMinMax) {
                        dialog.mProgressMinMax.setVisibility(View.VISIBLE);
                        dialog.mProgressMinMax.setText(String.format(builder.progressNumberFormat,
                                0, builder.progressMax));
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) dialog.mProgress.getLayoutParams();
                        lp.leftMargin = 0;
                        lp.rightMargin = 0;
                    } else {
                        dialog.mProgressMinMax.setVisibility(View.GONE);
                    }
                } else {
                    builder.showMinMax = false;
                }
            }
        }
    }

    private static void setupInputDialog(final TalkDialog dialog) {
        final TalkDialog.Builder builder = dialog.mBuilder;
        dialog.input = (EditText) dialog.view.findViewById(android.R.id.input);
        if (dialog.input == null) return;
        dialog.setTypeface(dialog.input, builder.regularFont);
        if (builder.inputPrefill != null)
            dialog.input.setText(builder.inputPrefill);
        dialog.setInternalInputCallback();
        dialog.input.setHint(builder.inputHint);
        dialog.input.setSingleLine();
        dialog.input.setTextColor(builder.contentColor);
        dialog.input.setHintTextColor(DialogUtils.adjustAlpha(builder.contentColor, 0.3f));
        MDTintHelper.setTint(dialog.input, dialog.mBuilder.widgetColor);

        if (builder.inputType != -1) {
            dialog.input.setInputType(builder.inputType);
            if ((builder.inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                // If the flags contain TYPE_TEXT_VARIATION_PASSWORD, apply the password transformation method automatically
                dialog.input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        }

        dialog.inputMinMax = (TextView) dialog.view.findViewById(R.id.minMax);
        if (builder.inputMaxLength > -1) {
            dialog.invalidateInputMinMaxIndicator(dialog.input.getText().toString().length(),
                    !builder.inputAllowEmpty);
        } else {
            dialog.inputMinMax.setVisibility(View.GONE);
            dialog.inputMinMax = null;
        }
    }
}
