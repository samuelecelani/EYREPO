import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'piao-icon',
  imports: [],
  templateUrl: './icon.component.html',
  styleUrl: './icon.component.scss',
})
export class IconComponent implements OnInit {
  @Input() icon!: string;
  @Input() iconContainerClass: string = 'icon-container';
  @Input() iconImgClass: string = 'icon-img';
  @Input() iconColor: string = 'black';
  iconPath: string = '/assets/icon/';
  fileExtension: string = '.svg';

  ngOnInit(): void {
    if (this.icon) this.iconPath = this.iconPath + this.icon + this.fileExtension;
  }
}
