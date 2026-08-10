from flask import Flask, request, jsonify
import face_recognition
import numpy as np
import pickle
import os

app = Flask(__name__)

ENCODINGS_FILE = "encodings.pkl"


def load_encodings():
    if os.path.exists(ENCODINGS_FILE):
        with open(ENCODINGS_FILE, "rb") as f:
            return pickle.load(f)
    return {"encodings": [], "names": []}


def save_encodings(data):
    with open(ENCODINGS_FILE, "wb") as f:
        pickle.dump(data, f)


@app.route("/enroll", methods=["POST"])
def enroll():
    if "image" not in request.files:
        return jsonify({"error": "No image provided"}), 400

    name = request.form.get("name")
    if not name:
        return jsonify({"error": "No name provided"}), 400

    image_file = request.files["image"]
    image = face_recognition.load_image_file(image_file)

    face_encodings = face_recognition.face_encodings(image)

    if len(face_encodings) == 0:
        return jsonify({"error": "No face detected in image"}), 400

    if len(face_encodings) > 1:
        return jsonify({"error": "Multiple faces detected, please provide an image with one face"}), 400

    data = load_encodings()
    data["encodings"].append(face_encodings[0])
    data["names"].append(name)
    save_encodings(data)

    return jsonify({"message": f"Enrolled {name} successfully"}), 200


@app.route("/recognize", methods=["POST"])
def recognize():
    if "image" not in request.files:
        return jsonify({"error": "No image provided"}), 400

    image_file = request.files["image"]
    image = face_recognition.load_image_file(image_file)

    face_locations = face_recognition.face_locations(image)
    face_encodings = face_recognition.face_encodings(image, face_locations)

    if len(face_encodings) == 0:
        return jsonify({"recognized": False, "message": "No face detected"}), 200

    data = load_encodings()

    if len(data["encodings"]) == 0:
        return jsonify({"recognized": False, "message": "No enrolled faces to compare against"}), 200

    results = []

    for face_encoding in face_encodings:
        face_distances = face_recognition.face_distance(data["encodings"], face_encoding)
        best_match_index = int(np.argmin(face_distances))
        best_distance = float(face_distances[best_match_index])

        if best_distance <= 0.5:
            results.append({
                "name": data["names"][best_match_index],
                "confidence": round(1 - best_distance, 2),
                "recognized": True
            })
        else:
            results.append({
                "name": None,
                "confidence": round(1 - best_distance, 2),
                "recognized": False
            })

    return jsonify({"faces": results}), 200


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)