Component({
  properties: {
    visible: { type: Boolean, value: false },
    show: { type: Boolean, value: false },
    mapId: { type: String, value: "resultMap" },
    mapLat: { type: Number, value: 35 },
    mapLng: { type: Number, value: 105 },
    mapScale: { type: Number, value: 12 },
    markers: { type: Array, value: [] },
    polyline: { type: Array, value: [] },
    guessCity: { type: String, value: "" },
    answerLabel: { type: String, value: "" },
    distanceLabel: { type: String, value: "" },
    dynastyScore: { type: Number, value: 0 },
    geoScore: { type: Number, value: 0 },
    totalScore: { type: Number, value: 0 },
    eventTitle: { type: String, value: "" },
    eventTime: { type: String, value: "" },
    knowledgeTitle: { type: String, value: "" },
    knowledgeText: { type: String, value: "" },
    knowledgeImage: { type: String, value: "" },
    showClose: { type: Boolean, value: false },
    nextLabel: { type: String, value: "" },
  },

  methods: {
    onClose: function () {
      this.triggerEvent("close");
    },
    onNext: function () {
      this.triggerEvent("next");
    },
    onPreviewImage: function () {
      this.triggerEvent("previewimage", { url: this.properties.knowledgeImage });
    },
  },
});
