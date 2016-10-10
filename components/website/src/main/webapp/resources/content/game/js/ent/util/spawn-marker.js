{
    var entity = function()
    {
        Entity.call(this,
            {
                title: "Spawn Marker",
                model: "2d-rect",
                width: 32.0,
                height: 32.0
            }
        );

        this.setTexture("white");
        this.setColour(1.0, 0.0, 0.0, 0.8);
    };

    entity.typeId = 902;
    entity.mapEditorEnabled = true;

    entity.inherits(Entity);

    game.entities.util.SpawnMarker = entity;
}
