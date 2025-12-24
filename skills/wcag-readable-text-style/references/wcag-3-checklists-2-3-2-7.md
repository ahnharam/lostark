# WCAG 3.0 체크리스트: 2.3~2.7 (포커스/입력/오류/모션/레이아웃)

> **출처**: https://www.w3.org/TR/wcag-3.0/  
> **상태**: Draft (초안)  
> **주의**: 항목 문구는 WCAG 3.0 원문(영문)을 그대로 정리함. 번역/해석은 별도 검토 필요.

---

## 2.3 Interactive components

### 2.3.1 Keyboard focus appearance
- [ ] Guideline: Users can see which element has keyboard focus.
- [ ] Custom indicator: A custom focus indicator is used with sufficient size, change of contrast, adjacent contrast, distinct style and adjacency.
- [ ] User agent default indicator: Focusable item uses the user agent default indicator.

### 2.3.2 Pointer focus appearance
- [ ] Guideline: Users can see the location of the pointer focus.

### 2.3.3 Navigating content
- [ ] Guideline: Users can determine where they are and move through content (including interactive elements) in a systematic and meaningful way regardless of input or movement method.

### 2.3.4 Expected behavior
- [ ] Guideline: Users have interactive components that behave as expected.

### 2.3.5 Control information
- [ ] Guideline: Users have information about interactive components that is identifiable and usable visually and using assistive technology.

---

## 2.4 Input / operation

### 2.4.1 Keyboard interface input
- [ ] Guideline: Users can navigate and operate content using only the keyboard.
- [ ] All elements keyboard actionable: All elements that can be controlled or activated by pointer, audio (voice or other), gesture, camera input, or other means can be controlled or activated from the keyboard interface.
- [ ] All content keyboard accessible: All content that can be accessed by other input modalities can be accessed using keyboard interface only.
- [ ] Bidirectional navigation: It is always possible to move forward and backward at each point using keyboard navigation.
- [ ] Conflicting keyboard commands: Author-generated keyboard commands do not conflict with standard platform keyboard commands or they can be remapped.
- [ ] Keyboard navigable if responsive: If the page/view uses responsive design, the page/view remains fully keyboard navigable.
- [ ] No keyboard trap: It is always possible to navigate away from an element after navigating to, entering, or activating the element by using a common keyboard navigation technique, or by using a technique described on the page/view or on a page/view earlier in the process where it is used.
- [ ] User control of keyboard focus: When the keyboard focus is moved, one of the following is true:
  - [ ] The focus was moved under direct user control;
  - [ ] A new view, such as a dialog, is introduced and focus is moved to that view;
  - [ ] The user is informed of the potential keyboard focus move before it happens and given the chance to avoid the move;
  - [ ] The keyboard focus moves to the next item in keyboard navigation order automatically on completion of some user action.
- [ ] Relevant tab order keyboard focus: Except for skip links and other elements that are hidden but specifically added to aid keyboard navigation, tabbing does not move the keyboard focus into content that was not visible before the tab action.

### 2.4.2 Physical or cognitive effort when using keyboard
- [ ] Guideline: Users can use keyboard without unnecessary physical or cognitive effort.
- [ ] Logical keyboard focus order: The keyboard focus moves through content in an order and way that preserves meaning and operability.
- [ ] Preserve keyboard focus: When keyboard focus moves from one context to another within a page/view, whether automatically or by user request, the keyboard focus is preserved so that, when the user returns to the previous context, the keyboard focus is restored to its previous location unless that location no longer exists.
- [ ] Comparable keyboard effort: Content author(s) follow user interface design principles that include minimizing the difference between the number of input commands required when using the keyboard interface only and the number of commands when using other input modalities.

### 2.4.3 Pointer input
- [ ] Guideline: Pointer input is consistent and all functionality can be done with simple pointer input in a time and pressure insensitive way.
- [ ] Pointer cancellation: For functionality that can be activated using a simple pointer input, at least one of the following is true:
  - [ ] No Down Event: The down event of the pointer is not used to execute any part of the function.
  - [ ] Abort or Undo: Completion of the function is on the up event, and a mechanism is available to abort the function before completion or to undo the function after completion.
  - [ ] Up Reversal: The up event reverses any outcome of the preceding down event.
  - [ ] Essential: Completing the function on the down event is essential.
- [ ] Simple pointer input: Any functionality that uses pointer input other than simple pointer input can also be operated by a simple pointer input or a sequence of simple pointer inputs that do not require timing.
- [ ] Consistent pointer cancellation: The method of pointer cancellation is consistent for each type of interaction within a set of pages/views except where it is essential to be different.
- [ ] Pointer pressure alternative: Specific pointer pressure is not the only way of achieving any functionality, except where specific pressure is essential to the functionality.
- [ ] Pointer speed alternative: Specific pointer speed is not the only way of achieving any functionality, except where specific pointer speed is essential to the functionality.

### 2.4.4 Speech and voice input
- [ ] Guideline: Provide alternatives to speech input and facilitate speech control.
- [ ] Speech alternative: Speech input is not the only way of achieving any functionality except where a speech input is essential to the functionality.
- [ ] Real-time bidirectional voice communication: Wherever there is real-time bidirectional voice communication, a real-time text option is available.

### 2.4.5 Input operation
- [ ] Guideline: Users have the option to use different input techniques and combinations and switch between them.
- [ ] Change keyboard focus with pointer device: If content interferes with pointer or keyboard focus behavior of the user agent, then selecting anything on the view with a pointer moves the keyboard focus to that interactive element, even if the user drags off the element (so as to not activate it).
- [ ] Content on hover or keyboard focus: When receiving and then removing pointer hover or keyboard focus triggers additional content to become visible and then hidden, and the visual presentation of the additional content is controlled by the author and not by the user agent, all of the following are true:
  - [ ] Dismissible: A mechanism is available to dismiss the additional content without moving pointer hover or keyboard focus, unless the additional content does not obscure or replace other content.
  - [ ] Hoverable: If pointer hover can trigger the additional content, then the pointer can be moved over the additional content without the additional content disappearing.
  - [ ] Persistent: The additional content remains visible until the hover or keyboard focus trigger is removed, the user dismisses it, or its information is no longer valid.
- [ ] Gesture alternative: Gestures are not the only way of achieving any functionality, except where a gesture is essential to the functionality.
- [ ] Input method flexibility: Where functionality, including input or navigation, is achievable using different input methods, users have the option to switch between those input methods at any time.
- [ ] Use without body movement: Full or gross body movement is not the only way of achieving any functionality, except where full or gross body movement is essential to the functionality.

### 2.4.6 Authentication
- [ ] Guideline: Users have alternative authentication methods available to them.
- [ ] Biometric identification: Biometric identification is not the only way to identify or authenticate.
- [ ] Voice identification: Voice identification is not the only way to identify or authenticate.

---

## 2.5 Error handling

### 2.5.1 Correct errors
- [ ] Guideline: Users know about and can correct errors.
- [ ] Error notification: When an error is detected, users are notified visually and programatically that an error has occurred.
- [ ] Error cause identified: Content in error is programmatically indicated.
- [ ] Descriptive errors: Error messages clearly describe the problem.
- [ ] Error cause in notification: When an error occurs due to a user interaction with an interactive element, the error message includes the human readable name of the element in error. If the interactive element is located in a different part of a process, then the page/view or step in the process is included in the error message.
- [ ] Error suggestion: Error messages includes suggestions for correction that can be automatically determined, unless it would jeopardize the security or purpose of the content.
- [ ] Error visibility: Error messages are visually identifiable including at least two of the following:
  - [ ] A symbol.
  - [ ] Color that differentiates the error message from surrounding content.
  - [ ] Text that clearly indicates this is an error.
- [ ] Error persists: Error messages persist until the user dismisses them or the error is resolved.
- [ ] Error cause association: Error messages are programmatically associated with the error source.
- [ ] Error linked: When an error notification is not adjacent to the item in error, a link to the error is provided.
- [ ] Error location: Error messages are visually collocated with the error source.

### 2.5.2 Prevent errors
- [ ] Guideline: When users are submitting information, at least one of the following is true:
  - [ ] Users can review, confirm, and correct information before submitting.
  - [ ] Information is validated and users can correct any errors found.
  - [ ] Users can undo submissions.
- [ ] Submission confirmation: On submission, users are notified of submitted information and submission status.
- [ ] Validate after data entry: During data entry, ensure data validation occurs after the user enters data and before the form is submitted.
- [ ] Validate as you go: When completing a multi-step process, validation is completed before the user moves to the next step in the process.

---

## 2.6 Animation and movement

### 2.6.1 Avoid physical harm
- [ ] Guideline: Users do not experience physical harm from content.
- [ ] Avoid audio shifting: Content does not include audio shifting designed to create a perception of motion, or it can be paused or prevented.
- [ ] Avoid flashing: Content does not include non-essential flashing or strobing beyond flashing thresholds.
- [ ] Flashing warning: When flashing is essential, a trigger warning is provided to inform users that flashing exists, and a mechanism is available to access the same information and avoid the flashing content.
- [ ] No flashing: Content does not include flashing.
- [ ] Avoid visual motion: Content does not include non-essential visual motion lasting longer than 5 seconds and pseudo-motion.
- [ ] Visual motion warning: When visual motion lasting longer than 5 seconds or pseudo-motion is essential, a trigger warning is provided to inform users that such content exists, and users are provided a way to access the same information and avoid the visual motion or pseudo-motion.
- [ ] No visual motion: Content does not include visual motion lasting longer than 5 seconds or pseudo-motion.
- [ ] Avoid motion from interaction: Content does not include non-essential visual motion and pseudo-motion triggered by interaction unless it can be paused or prevented.

---

## 2.7 Layout

### 2.7.1 Relationships
- [ ] Guideline: Users can determine relationships between content both visually and using assistive technologies.

### 2.7.2 Recognizable layouts
- [ ] Guideline: Users have consistent and recognizable layouts available.

### 2.7.3 Orientation
- [ ] Guideline: Users can determine their location in content both visually and using assistive technologies.

### 2.7.4 Structure
- [ ] Guideline: Users can understand and navigate through the content using structure.
- [ ] Programmatic relationships: Relationship between elements are conveyed programmatically.
- [ ] Regions: Elements are programmatically grouped together within landmarks.
- [ ] Section labels: Groups of elements have a label that defines their purpose.
- [ ] Heading structure: Groups of elements are organized with a logical and meaningful hierarchy of headings.
- [ ] Lists: Lists are visually and programmatically identifiable as a collection of related items.
- [ ] Visually organized: Related elements are visually grouped together.

### 2.7.5 No obstruction
- [ ] Guideline: Users can perceive and operate user interface components and navigation without obstruction.
- [ ] No obstructions: Content that is essential for a user’s task or understanding is not permanently covered by non-dismissible or non-movable elements.
- [ ] Clearly-dismissible content overlays: When content temporarily overlays other content, it must be clearly dismissible or movable via standard interaction methods and its presence does not disrupt critical screen reader announcements or keyboard focus.
- [ ] Disabled controls: If a control is disabled, then information explaining why it is disabled and what actions are needed to enable it is provided visually and programmatically.
- [ ] Consistent positioning: Elements designed to be visually persistent have predictable positions and do not overlap with primary content in a way that makes it unreadable or unusable.
