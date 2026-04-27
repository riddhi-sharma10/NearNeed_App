# NearNeed XML Terms and Meanings (Complete Guide)

This glossary covers XML terms used throughout the app, including:

- layout/view terms,
- Material component terms,
- drawable/vector/animation terms,
- values/resource container terms,
- AndroidManifest terms.

---

## 1. XML Namespaces and Prefix Terms

| Term | Meaning |
|---|---|
| android: | Standard Android XML attribute namespace. |
| app: | Library/custom attributes namespace (ConstraintLayout, Material, etc.). |
| tools: | Design-time preview/testing attributes in Android Studio (not used at runtime). |
| aapt:attr | Special resource packaging attribute wrapper used in adaptive icon XML. |

---

## 2. Layout and View Terms

| Term | Meaning |
|---|---|
| LinearLayout | Places child views in a single row or column. |
| RelativeLayout | Places child views relative to parent or sibling views. |
| FrameLayout | Stacks child views on top of each other. |
| GridLayout | Places child views in a grid (rows and columns). |
| ScrollView | Vertical scrolling container for one direct child. |
| HorizontalScrollView | Horizontal scrolling container for one direct child. |
| androidx.core.widget.NestedScrollView | Scroll container that cooperates with nested scrolling parents/children. |
| androidx.constraintlayout.widget.ConstraintLayout | Flexible layout where views are positioned using constraints. |
| androidx.constraintlayout.widget.Guideline | Invisible guide line used to align/constrain views in ConstraintLayout. |
| androidx.coordinatorlayout.widget.CoordinatorLayout | Parent layout that coordinates behavior between child views (AppBar, BottomSheet, FAB). |
| androidx.fragment.app.FragmentContainerView | Container used to host Fragment UI. |
| androidx.recyclerview.widget.RecyclerView | Efficient, reusable list/grid container for large dynamic data. |
| androidx.viewpager2.widget.ViewPager2 | Swipe-able pager for pages/fragments. |
| View | Basic rectangular UI element, often used as spacer/divider/background block. |
| Space | Empty spacer view to create fixed spacing in layouts. |
| include | Reuses another XML layout inside current layout. |
| merge | Flattens included layout hierarchy to avoid extra wrapper views (conceptual term; use when needed). |
| ViewFlipper | Switches between multiple child views with animations. |

---

## 3. Basic Input and Display Widget Terms

| Term | Meaning |
|---|---|
| TextView | Displays read-only text. |
| EditText | Text input field for user typing. |
| ImageView | Displays drawable/image resources. |
| ImageButton | Button that uses an image as its content. |
| Button | Standard clickable text button. |
| CheckBox | Two-state checked/unchecked control. |
| RadioButton | Single-choice option, usually used with a group. |
| ProgressBar | Shows loading/progress state. |
| SeekBar | Draggable slider for numeric value selection. |
| RatingBar | Star-style rating input/view. |

---

## 4. AppCompat and Card UI Terms

| Term | Meaning |
|---|---|
| androidx.appcompat.widget.Toolbar | Customizable top app bar. |
| androidx.cardview.widget.CardView | Rectangular card container with corner radius/shadow. |

---

## 5. Material Component Terms

| Term | Meaning |
|---|---|
| com.google.android.material.appbar.AppBarLayout | Vertical layout used for app bars and scroll-linked behavior. |
| com.google.android.material.button.MaterialButton | Material Design button with advanced styling support. |
| com.google.android.material.card.MaterialCardView | Material card container with stroke, elevation, shape support. |
| com.google.android.material.chip.Chip | Compact selectable/filter/action element. |
| com.google.android.material.chip.ChipGroup | Parent that arranges and manages Chips. |
| com.google.android.material.floatingactionbutton.FloatingActionButton | Circular floating action button for primary actions. |
| com.google.android.material.imageview.ShapeableImageView | ImageView with Material shape/corner customization. |
| com.google.android.material.materialswitch.MaterialSwitch | Material-styled switch toggle. |
| com.google.android.material.switchmaterial.SwitchMaterial | Material switch variant from SwitchMaterial class. |
| com.google.android.material.progressindicator.LinearProgressIndicator | Material linear progress bar. |
| com.google.android.material.slider.Slider | Material value slider control. |
| com.google.android.material.tabs.TabLayout | Horizontal tabs for section/page switching. |
| com.google.android.material.textfield.TextInputLayout | Wrapper that adds hint/error/icons around text input fields. |
| com.google.android.material.textfield.TextInputEditText | EditText designed to work inside TextInputLayout. |

---

## 6. Maps and Specialized View Terms

| Term | Meaning |
|---|---|
| org.maplibre.android.maps.MapView | Interactive map view provided by MapLibre SDK. |

---

## 7. Drawable, Shape, Ripple, and Vector Terms

| Term | Meaning |
|---|---|
| selector | State-based drawable switcher (different visuals for pressed/checked/selected states). |
| item | Entry inside selector/layer-list/menu/array and similar container tags. |
| layer-list | Stacks multiple drawables on top of each other. |
| shape | Defines geometric drawable (rectangle/oval/line/ring) with color/stroke/corners. |
| solid | Fill color block inside shape drawable. |
| stroke | Border line definition for shape/vector paths. |
| corners | Corner radius settings inside shape drawable. |
| size | Explicit width/height for shape drawable. |
| padding | Internal spacing for shape drawable content. |
| gradient | Color gradient fill definition. |
| ripple | Touch ripple effect drawable. |
| inset | (attribute use) draws child drawable with inset margins. |
| clip | Drawable clipping instruction (often in level/shape drawables). |
| scale | Drawable scaling wrapper instruction. |
| vector | Vector drawable root tag. |
| path | Drawing path inside vector drawable. |
| clip-path | Vector clipping path. |
| group | Grouping/transform node in vector drawables. |
| alpha | Opacity control block/value in drawable animation/vector context. |
| foreground | Foreground drawable/layer placed above content. |
| background | Background drawable/layer behind content. |

---

## 8. Animation and Transition Terms

| Term | Meaning |
|---|---|
| set | Container that groups multiple animations together. |
| translate | Moves a view/drawable from one position to another. |
| scale | Grows/shrinks a view/drawable. |
| alpha | Fades view/drawable in or out. |

---

## 9. Resource Container Terms

| Term | Meaning |
|---|---|
| resources | Root container for Android resource definitions. |
| string | Single text resource value. |
| color | Color resource value. |
| dimen | Dimension resource value (dp/sp/etc.). |
| style | Reusable style/theme block for views. |
| array | List resource container (strings/ints/etc.). |
| menu | Root for menu resource definitions. |
| adaptive-icon | Adaptive launcher icon definition. |
| monochrome | Monochrome icon layer for adaptive icons (Android 13+). |

---

## 10. Backup and Data Rules XML Terms

| Term | Meaning |
|---|---|
| data-extraction-rules | Root tag for Android data extraction/backup policy rules. |
| cloud-backup | Defines what is backed up to cloud backup. |
| device-transfer | Defines data included in device-to-device transfer. |
| full-backup-content | Legacy full backup rules root. |
| include | Include matching files/data in backup policy. |
| exclude | Exclude matching files/data in backup policy. |

---

## 11. AndroidManifest XML Terms

| Term | Meaning |
|---|---|
| manifest | Root of AndroidManifest file. |
| application | App-wide metadata and component declarations. |
| activity | Screen component declaration. |
| service | Background component declaration. |
| intent-filter | Declares intents a component can handle. |
| action | Action string inside intent filter. |
| category | Category string inside intent filter. |
| uses-permission | Declares required app permission. |
| uses-feature | Declares optional/required hardware/software feature. |
| queries | Declares package visibility queries (Android 11+). |
| package | Package query item inside queries block. |
| meta-data | Additional key/value metadata for app/components/libraries. |

---

## 12. Most Important XML Attributes You Use (Quick Meaning)

These are the most frequently used XML attribute terms across your app:

| Term | Meaning |
|---|---|
| android:id | Unique view/resource identifier. |
| android:layout_width | Width of a view. |
| android:layout_height | Height of a view. |
| android:layout_weight | Shares extra space proportionally in LinearLayout. |
| android:orientation | Direction of child arrangement (horizontal/vertical). |
| android:gravity | Aligns content inside a view. |
| android:layout_gravity | Aligns the view itself inside its parent slot. |
| android:padding / margin | Internal space / external space around a view. |
| android:text | Text value shown by text-based view. |
| android:hint | Placeholder text for input fields. |
| android:textColor | Text color. |
| android:textSize | Text size. |
| android:fontFamily | Font family selection. |
| android:background | Background drawable/color. |
| android:src | Image/drawable source for ImageView-like views. |
| android:tint | Color tint applied to drawable/icon. |
| android:clickable | Whether view can be clicked. |
| android:focusable | Whether view can receive focus. |
| android:visibility | View visibility state (visible/invisible/gone). |
| android:inputType | Keyboard/input behavior for EditText. |
| android:imeOptions | IME action behavior (done/search/next). |
| app:layout_constraintStart_toStartOf (and other constraint attrs) | Defines positional constraints in ConstraintLayout. |
| app:cardCornerRadius | Corner radius for Card/MaterialCard. |
| app:cardElevation | Shadow elevation for Card/MaterialCard. |
| app:strokeColor / app:strokeWidth | Border color/width on MaterialCard/Button/shape-supporting widgets. |
| app:backgroundTint | Tint color for Material widget background. |
| app:icon / app:iconTint | Icon and icon tint for Material buttons/text fields. |
| app:boxBackgroundMode | TextInputLayout box mode (filled/outlined). |
| app:boxStrokeColor | TextInputLayout outline color. |
| app:layout_behavior | CoordinatorLayout child behavior (e.g., BottomSheet behavior). |
| tools:context | Preview hint for Android Studio editor. |
| tools:text / tools:src / tools:visibility | Preview-only sample values in design editor. |

---

## 13. Notes for Viva

- If examiner asks LinearLayout vs ConstraintLayout:
  - LinearLayout is simple and readable for row/column stacking.
  - ConstraintLayout is better for complex responsive positioning with flatter hierarchy.

- If examiner asks TextInputLayout vs EditText:
  - TextInputLayout provides Material wrapper features (error, icons, animated hint).
  - TextInputEditText/EditText is the actual text input control.

- If examiner asks RecyclerView:
  - It is a performance-optimized reusable list container, much better than static repeated layout blocks.

- If examiner asks selector/ripple:
  - selector changes drawable by state, ripple adds touch feedback animation.

---

If you want, I can also generate a second file named xml_attributes_full_dictionary.md that explains each individual attribute term from your project one by one (including all android:, app:, and tools: attributes).