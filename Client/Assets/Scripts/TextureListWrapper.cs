using System;
using System.Collections.Generic;
using UnityEngine;
using Serving.FileTransferring;

namespace Gridia
{
    public class TextureListWrapper {
        public List<Texture2D> Textures { get; private set; }
        public int Count { get { return Textures.Count; } }
        public String Prefix { get; private set; }
        private readonly FileSystem _fileSystem;

        public TextureListWrapper(String prefix, FileSystem fileSystem)
        {
            Prefix = prefix;
            _fileSystem = fileSystem;
            Textures = new List<Texture2D>();
        }

        public void LoadAll()
        {
            int index = 0;
            while (_fileSystem.Exists(Prefix + index + ".png"))
            {
                LoadTexture(index);
                index++;
            }
        }

        public Sprite GetSprite(int spriteIndex, int width = 1, int height = 1)
        {
            var tex = GetTextureForSprite(spriteIndex);
            var x = (spriteIndex%GridiaConstants.SpritesInSheet)%GridiaConstants.NumTilesInSpritesheetRow;
            var y = 10 - (spriteIndex%GridiaConstants.SpritesInSheet)/GridiaConstants.NumTilesInSpritesheetRow - height; // ?
            return Sprite.Create(tex, new Rect(x*32, y*32, 32*width, 32*height), new Vector2(0.5f, 0.5f), 1);
        }

        public Texture2D GetTexture(int textureIndex) 
        {
            if (Count <= textureIndex || Textures[textureIndex] == null)
            {
                LoadTexture(textureIndex);
            }

            return Textures[textureIndex];
        }

        public Texture2D GetTextureForSprite(int spriteIndex) 
        {
            var textureIndex = spriteIndex / GridiaConstants.SpritesInSheet;
            return GetTexture(textureIndex);
        }

        private void LoadTexture(int index)
        {
            var path = Prefix + index + ".png";
            Debug.Log("Loading texture: " + path);
            var data = _fileSystem.ReadAllBytes(path);
            var tex = new Texture2D(320, 320)
            {
                filterMode = FilterMode.Point,
                wrapMode = TextureWrapMode.Clamp
            };
            tex.LoadImage(data);
            InsertIntoList(Textures, tex, index);
        }

        private void InsertIntoList<T>(List<T> list, T texture, int index)
        {
            if (list.Count <= index)
            {
                for (var i = list.Count; i <= index; i++) 
                {
                    Textures.Add(null);
                }
            }
            list[index] = texture;
        }
    }
}
