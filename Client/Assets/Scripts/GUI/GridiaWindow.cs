﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

namespace Gridia 
{
    public abstract class GridiaWindow
    {
        private static int _NEXT_WINDOW_ID;

        public int WindowId { get; private set; }
        public bool MouseOver { get; private set; }
        public bool ResizingWindow { get; private set; }
        public bool ResizeOnHorizontal { get; set; }
        public bool ResizeOnVertical { get; set; }
        public float BorderSize { get; set; }
        public String WindowName { get; set; }
        protected Rect WindowRect { get; set; }

        public GridiaWindow(Vector2 position, String windowName)
        {
            WindowId = _NEXT_WINDOW_ID++;
            WindowName = windowName;
            WindowRect = new Rect(position.x, position.y, 300, 300);
            ResizeOnHorizontal = ResizeOnVertical = true;
            BorderSize = 20;
        }

        protected abstract void RenderContents();

        public virtual void Render() 
        {
            if (Event.current.type == EventType.Layout)
            {
                MouseOver = false;
            }
            if (Event.current.type == EventType.MouseUp)
            {
                ResizingWindow = false;
            }
            if (ResizingWindow)
            {
                Resize();
            }

            MouseOver = WindowRect.Contains(Event.current.mousePosition);

            WindowRect = GUI.Window(WindowId, WindowRect, windowId =>
            {
                RenderDragAndResize();
                GUILayout.BeginArea(new Rect(BorderSize, BorderSize, Int32.MaxValue, Int32.MaxValue));
                RenderContents();
                GUILayout.EndArea();
            }, WindowName);

            if (!ResizingWindow) 
            {
                ClampPosition();
            }
        }

        protected virtual void Resize()
        {
            var resized = WindowRect;
            if (ResizeOnHorizontal)
            {
                resized.width = Event.current.mousePosition.x - resized.x + BorderSize * 2;
                resized.width = Mathf.Clamp(resized.width, BorderSize * 2, Screen.width - WindowRect.x);
            }
            if (ResizeOnVertical)
            {
                resized.height = Event.current.mousePosition.y - resized.y + BorderSize * 2;
                resized.height = Mathf.Clamp(resized.height, BorderSize * 2, Screen.height - WindowRect.y);
            }
            WindowRect = resized;
        }

        protected void RenderDragAndResize()
        {
            GUI.DragWindow(new Rect(0, 0, WindowRect.width - 40, 20));
            var resizeRect = new Rect(WindowRect.width - 40, WindowRect.height - 20, 40, 20);
            if (Event.current.type == EventType.mouseDown && resizeRect.Contains(Event.current.mousePosition))
            {
                ResizingWindow = true;
            }
            GUI.Label(resizeRect, " ◄►");
        }

        private void ClampPosition()
        {
            var maxX = Math.Max(0, Screen.width - WindowRect.width);
            var maxY = Math.Max(0, Screen.height - WindowRect.height);
            var clamped = WindowRect;
            clamped.x = Mathf.Clamp(clamped.x, 0, maxX);
            clamped.y = Mathf.Clamp(clamped.y, 0, maxY);
            WindowRect = clamped;
        }
    }
}