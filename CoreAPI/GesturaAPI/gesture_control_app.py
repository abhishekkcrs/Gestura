import sys
import json
import os
import mediapipe as mp
import cv2
import numpy as np
from PyQt6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                            QHBoxLayout, QLabel, QPushButton, QComboBox, 
                            QTableWidget, QTableWidgetItem, QMessageBox)
from PyQt6.QtCore import Qt, QTimer, pyqtSignal, QThread
from PyQt6.QtGui import QImage, QPixmap
import pyautogui
import keyboard
from fastdtw import fastdtw
from scipy.spatial.distance import euclidean

class GestureRecognitionThread(QThread):
    gesture_detected = pyqtSignal(str)
    
    def __init__(self, gesture_dict):
        super().__init__()
        self.gesture_dict = gesture_dict
        self.running = True
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(min_detection_confidence=0.7, min_tracking_confidence=0.7)
        self.keypoints_to_check = [0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20]
        self.pending_gesture = None
        self.frame_stage = 0
        self.stage_start_time = None
        self.STAGE_TIMEOUT = 5
        self.COOLDOWN_TIME = 1
        self.last_detection_time = 0
        
    def normalize_landmarks(self, landmarks):
        min_x, min_y, _ = np.min(landmarks, axis=0)
        max_x, max_y, _ = np.max(landmarks, axis=0)
        center_x = (min_x + max_x) / 2
        center_y = (min_y + max_y) / 2
        centered_landmarks = landmarks - np.array([center_x, center_y, 0])
        hand_width, hand_height = max_x - min_x, max_y - min_y
        scale = max(hand_width, hand_height)
        if scale > 0:
            centered_landmarks /= scale
        return centered_landmarks

    def run(self):
        cap = cv2.VideoCapture(2)
        while self.running:
            ret, frame = cap.read()
            if not ret:
                continue

            frame = cv2.resize(frame, (320, 240))
            rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = self.hands.process(rgb_frame)
            current_time = time.time()

            if current_time - self.last_detection_time < self.COOLDOWN_TIME:
                continue

            if self.frame_stage > 0 and self.stage_start_time is not None:
                if current_time - self.stage_start_time > self.STAGE_TIMEOUT:
                    self.pending_gesture = None
                    self.frame_stage = 0
                    self.stage_start_time = None

            if results.multi_hand_landmarks:
                for hand_landmarks in results.multi_hand_landmarks:
                    landmarks = np.array([(lm.x, lm.y, lm.z) for lm in hand_landmarks.landmark],
                                     dtype=np.float32)
                    if landmarks.shape[0] < max(self.keypoints_to_check):
                        continue

                    normalized_landmarks = self.normalize_landmarks(landmarks)
                    normalized_keypoints = normalized_landmarks[self.keypoints_to_check]
                    frame_sequence = ["start", "mid1", "mid2", "end"]

                    if self.frame_stage == 0:
                        for gesture_name, frames in self.gesture_dict.items():
                            keyframe_points = frames["start"].reshape(-1, 3)
                            if keyframe_points.shape != normalized_keypoints.shape:
                                continue
                            distance, _ = fastdtw(keyframe_points, normalized_keypoints, dist=euclidean)
                            if distance < 1:
                                self.pending_gesture = gesture_name
                                self.frame_stage = 1
                                self.stage_start_time = current_time
                                break

                    elif self.pending_gesture:
                        stage_name = frame_sequence[self.frame_stage]
                        keyframe_points = self.gesture_dict[self.pending_gesture][stage_name].reshape(-1, 3)
                        if keyframe_points.shape == normalized_keypoints.shape:
                            distance, _ = fastdtw(keyframe_points, normalized_keypoints, dist=euclidean)
                            if distance < 1:
                                self.frame_stage += 1
                                self.stage_start_time = current_time
                                if self.frame_stage == 3:
                                    self.gesture_detected.emit(self.pending_gesture)
                                    self.last_detection_time = current_time
                                    self.pending_gesture = None
                                    self.frame_stage = 0
                                    self.stage_start_time = None
                                    break

        cap.release()

    def stop(self):
        self.running = False

class GestureControlApp(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Gesture Control System")
        self.setGeometry(100, 100, 800, 600)
        
        # Load gesture mappings
        self.gesture_mappings = self.load_gesture_mappings()
        
        # Create main widget and layout
        main_widget = QWidget()
        self.setCentralWidget(main_widget)
        layout = QVBoxLayout(main_widget)
        
        # Create gesture mapping table
        self.create_mapping_table()
        layout.addWidget(self.mapping_table)
        
        # Create controls
        controls_layout = QHBoxLayout()
        
        self.gesture_combo = QComboBox()
        self.gesture_combo.addItems(self.gesture_mappings.keys())
        controls_layout.addWidget(QLabel("Gesture:"))
        controls_layout.addWidget(self.gesture_combo)
        
        self.action_combo = QComboBox()
        self.action_combo.addItems(["Open Chrome", "Pause Video", "Play Video", "Next Track", "Previous Track", "Volume Up", "Volume Down"])
        controls_layout.addWidget(QLabel("Action:"))
        controls_layout.addWidget(self.action_combo)
        
        add_button = QPushButton("Add Mapping")
        add_button.clicked.connect(self.add_mapping)
        controls_layout.addWidget(add_button)
        
        layout.addLayout(controls_layout)
        
        # Initialize gesture recognition
        self.gesture_thread = GestureRecognitionThread(self.load_gestures())
        self.gesture_thread.gesture_detected.connect(self.handle_gesture)
        self.gesture_thread.start()

    def load_gestures(self):
        if os.path.exists("gestures.json"):
            with open("gestures.json", "r") as f:
                gesture_dict = json.load(f)
                return {k: {frame: np.array(v, dtype=np.float32) 
                           for frame, v in frames.items()} 
                        for k, frames in gesture_dict.items()}
        return {}

    def load_gesture_mappings(self):
        if os.path.exists("gesture_mappings.json"):
            with open("gesture_mappings.json", "r") as f:
                return json.load(f)
        return {}

    def save_gesture_mappings(self):
        with open("gesture_mappings.json", "w") as f:
            json.dump(self.gesture_mappings, f)

    def create_mapping_table(self):
        self.mapping_table = QTableWidget()
        self.mapping_table.setColumnCount(2)
        self.mapping_table.setHorizontalHeaderLabels(["Gesture", "Action"])
        self.update_mapping_table()

    def update_mapping_table(self):
        self.mapping_table.setRowCount(len(self.gesture_mappings))
        for i, (gesture, action) in enumerate(self.gesture_mappings.items()):
            self.mapping_table.setItem(i, 0, QTableWidgetItem(gesture))
            self.mapping_table.setItem(i, 1, QTableWidgetItem(action))

    def add_mapping(self):
        gesture = self.gesture_combo.currentText()
        action = self.action_combo.currentText()
        self.gesture_mappings[gesture] = action
        self.save_gesture_mappings()
        self.update_mapping_table()

    def handle_gesture(self, gesture):
        if gesture in self.gesture_mappings:
            action = self.gesture_mappings[gesture]
            self.execute_action(action)

    def execute_action(self, action):
        if action == "Open Chrome":
            os.system("start chrome")
        elif action == "Pause Video":
            keyboard.press_and_release('space')
        elif action == "Play Video":
            keyboard.press_and_release('space')
        elif action == "Next Track":
            keyboard.press_and_release('next track')
        elif action == "Previous Track":
            keyboard.press_and_release('previous track')
        elif action == "Volume Up":
            keyboard.press_and_release('volume up')
        elif action == "Volume Down":
            keyboard.press_and_release('volume down')

    def closeEvent(self, event):
        self.gesture_thread.stop()
        self.gesture_thread.wait()
        event.accept()

if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = GestureControlApp()
    window.show()
    sys.exit(app.exec()) 